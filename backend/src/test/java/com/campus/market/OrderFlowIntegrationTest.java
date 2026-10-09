package com.campus.market;

import com.campus.market.entity.Enums.ProductStatus;
import com.campus.market.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.concurrent.*;
import java.util.Map;
import java.util.stream.IntStream;

@SpringBootTest @AutoConfigureMockMvc
@TestPropertySource(properties={"spring.datasource.url=jdbc:h2:mem:market-test;MODE=MySQL","spring.jpa.hibernate.ddl-auto=create-drop","app.upload-dir=target/test-uploads"})
class OrderFlowIntegrationTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired ProductRepository products;
    private String login(String username) throws Exception {
        String body=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\""+username+"\",\"password\":\"123456\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).path("data").path("token").asText();
    }
    @Test void completeOrderStateMachine() throws Exception {
        String buyer=login("student"),seller=login("seller");
        Long productId=products.findAll().stream().filter(p->p.seller.username.equals("seller")&&p.status==ProductStatus.ON_SALE).findFirst().orElseThrow().id;
        String created=mvc.perform(post("/api/orders").header("Authorization","Bearer "+buyer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":"+productId+",\"tradeLocation\":\"图书馆南门\",\"buyerNote\":\"集成测试订单\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("PENDING_CONFIRM")).andReturn().getResponse().getContentAsString();
        long id=json.readTree(created).path("data").path("id").asLong();
        action(id,"confirm",seller,"PENDING_PAYMENT"); action(id,"pay",buyer,"PENDING_DELIVERY");
        action(id,"deliver",seller,"PENDING_RECEIVE"); action(id,"receive",buyer,"COMPLETED");
        assertThat(products.findById(productId).orElseThrow().status).isEqualTo(ProductStatus.SOLD);
    }
    @Test void protectedApiUsesUnifiedUnauthorizedResponse() throws Exception {
        mvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101))
                .andExpect(jsonPath("$.message").value("请先登录"));
    }
    @Test void validatesProductPriceAndSupportsLocalImageUpload() throws Exception {
        String token=login("student");
        mvc.perform(post("/api/products").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"无效价格商品\",\"categoryId\":1,\"price\":-1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(40001));
        MockMultipartFile image=new MockMultipartFile("file","campus.png","image/png",new byte[]{1,2,3,4});
        mvc.perform(multipart("/api/uploads/images").file(image).header("Authorization","Bearer "+token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.url").value(org.hamcrest.Matchers.startsWith("/uploads/products/")));
    }
    @Test void normalizesPaginationAndRejectsInvalidPriceRange() throws Exception {
        mvc.perform(get("/api/products").param("page","-2").param("size","0"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.size").value(12));
        mvc.perform(get("/api/products").param("minPrice","100").param("maxPrice","10"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("最低价不能高于最高价"));
    }
    @Test void rejectsUnknownAdminActionsAndInactiveCategories() throws Exception {
        String admin=login("admin"),student=login("student");
        mvc.perform(post("/api/admin/users/1/toggle").header("Authorization","Bearer "+admin))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("不支持的用户操作"));
        String category=mvc.perform(post("/api/admin/categories").header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"回归测试停用分类\",\"active\":false}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long categoryId=json.readTree(category).path("data").path("id").asLong();
        mvc.perform(post("/api/products").header("Authorization","Bearer "+student).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"不应发布的商品\",\"categoryId\":"+categoryId+",\"price\":1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("所选分类已停用"));
    }
    @Test void storesFiveHundredCharacterReportDescription() throws Exception {
        String student=login("student");
        Long productId=products.findAll().stream().filter(p->p.seller.username.equals("seller")).findFirst().orElseThrow().id;
        String description="详".repeat(500);
        mvc.perform(post("/api/products/"+productId+"/reports").header("Authorization","Bearer "+student).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("reason","回归测试","description",description))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.description").value(description));
    }
    @Test void concurrentBuyersCannotCreateTwoOrdersForOneProduct() throws Exception {
        String buyerOne=login("student");
        String buyerTwo=login("admin");
        Long productId=products.findAll().stream().filter(p->p.seller.username.equals("seller")&&p.status==ProductStatus.ON_SALE).findFirst().orElseThrow().id;
        ExecutorService pool=Executors.newFixedThreadPool(2);
        CountDownLatch ready=new CountDownLatch(2),start=new CountDownLatch(1);
        Callable<Integer> first=()->createOrderConcurrently(productId,buyerOne,ready,start);
        Callable<Integer> second=()->createOrderConcurrently(productId,buyerTwo,ready,start);
        Future<Integer> one=pool.submit(first),two=pool.submit(second);
        ready.await(5,TimeUnit.SECONDS);start.countDown();
        int success=(one.get(10,TimeUnit.SECONDS)==200?1:0)+(two.get(10,TimeUnit.SECONDS)==200?1:0);
        pool.shutdownNow();
        assertThat(success).isEqualTo(1);
    }
    @Test void concurrentProductViewsAreNotLost() throws Exception {
        Long productId=products.findAll().stream().filter(p->p.status==ProductStatus.ON_SALE).findFirst().orElseThrow().id;
        int before=products.findById(productId).orElseThrow().viewCount;
        ExecutorService pool=Executors.newFixedThreadPool(8);
        try {
            var futures=IntStream.range(0,20).mapToObj(i->pool.submit(()->mvc.perform(get("/api/products/"+productId)).andReturn().getResponse().getStatus())).toList();
            for(Future<Integer> future:futures)assertThat(future.get(10,TimeUnit.SECONDS)).isEqualTo(200);
        } finally { pool.shutdownNow(); }
        assertThat(products.findById(productId).orElseThrow().viewCount).isEqualTo(before+20);
    }
    private int createOrderConcurrently(long productId,String token,CountDownLatch ready,CountDownLatch start) throws Exception {
        ready.countDown();start.await(5,TimeUnit.SECONDS);
        return mvc.perform(post("/api/orders").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":"+productId+"}")).andReturn().getResponse().getStatus();
    }
    private void action(long id,String action,String token,String expected) throws Exception {
        mvc.perform(post("/api/orders/"+id+"/"+action).header("Authorization","Bearer "+token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value(expected));
    }
}
