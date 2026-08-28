package com.campus.market.config;

import com.campus.market.entity.*;
import com.campus.market.entity.Enums.*;
import com.campus.market.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.*;

@Configuration
public class DemoDataConfig {
    @Bean CommandLineRunner seed(@Value("${app.seed-demo-data:true}")boolean enabled,UserRepository users,CategoryRepository cats,ProductRepository products,ReportRepository reports,PasswordEncoder encoder){return args->{
        if(!enabled||users.count()>0)return;
        User admin=user("admin","管理员","20260000",Role.ADMIN,encoder);admin.campus="平台运营中心";users.save(admin);
        User seller=user("seller","林小满","20260101",Role.USER,encoder);seller.campus="东校区";seller.dormArea="松园 3 栋";seller.bio="毕业季清理闲置，支持校内面交";users.save(seller);
        User buyer=user("student","陈同学","20260202",Role.USER,encoder);buyer.campus="西校区";buyer.dormArea="竹园 2 栋";users.save(buyer);
        String[] names={"教材书籍","数码产品","电脑配件","生活用品","宿舍用品","文体用品","服饰鞋包","交通工具","票券卡券","其他"};List<Category> list=new ArrayList<>();for(int i=0;i<names.length;i++){Category c=new Category();c.name=names[i];c.sortOrder=i;list.add(c);}cats.saveAll(list);
        add(products,seller,list.get(2),"RK87 机械键盘 · 茶轴","九成新机械键盘，键帽已清洁，配原装数据线。适合宿舍学习与游戏。","199","119","九成新","图书馆南门",101);
        add(products,seller,list.get(0),"高等数学同济第七版上下册","少量课堂笔记，无缺页，期末复习非常实用，两册一起出。","89","28","八成新","第三教学楼",102);
        add(products,seller,list.get(1),"小米降噪蓝牙耳机","使用半年，续航正常，耳塞配件齐全，已酒精消毒。","399","168","九成新","一食堂门口",103);
        add(products,buyer,list.get(5),"尤尼克斯羽毛球拍","入门级全碳素球拍，送拍套和两只训练球。","269","98","八成新","体育馆",104);
        add(products,seller,list.get(4),"宿舍折叠小桌板","尺寸 60×40cm，稳固无晃动，毕业带不走低价出。","69","25","七成新","松园 3 栋",105);
        add(products,buyer,list.get(7),"捷安特校园通勤自行车","变速、刹车均正常，可现场试骑，仅支持东校区面交。","1299","520","八成新","东校区北门",106);
        add(products,seller,list.get(3),"九阳迷你电煮锅","一人食容量，宿舍煮面很方便。包装和说明书都在。","109","55","九成新","松园生活区",107);
        Product bad=add(products,seller,list.get(9),"超低价全新手机","宣传信息待核验，请勿脱离平台沟通。","5999","999","全新","校门外",108);
        Report rp=new Report();rp.reporter=buyer;rp.product=bad;rp.reason="价格欺诈";rp.description="价格明显异常，描述信息不完整";reports.save(rp);
    };}
    private User user(String username,String nick,String no,Role role,PasswordEncoder e){User u=new User();u.username=username;u.nickname=nick;u.studentNo=no;u.passwordHash=e.encode("123456");u.role=role;return u;}
    private Product add(ProductRepository repo,User seller,Category c,String title,String desc,String original,String price,String condition,String location,int seed){Product p=new Product();p.seller=seller;p.category=c;p.title=title;p.description=desc;p.originalPrice=new BigDecimal(original);p.price=new BigDecimal(price);p.conditionLevel=condition;p.tradeType=TradeType.SALE;p.tradeLocation=location;p.imageUrls=List.of("https://picsum.photos/seed/campus"+seed+"/800/600");p.viewCount=seed%37+20;return repo.save(p);}
}
