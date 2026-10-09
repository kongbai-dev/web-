import {RotateCcw,Search,SlidersHorizontal} from 'lucide-react'
import {useEffect,useState} from 'react'
import {Link,useSearchParams} from 'react-router-dom'
import {api} from '../api'
import {useAuth} from '../AuthContext'
import ProductCard from '../components/ProductCard'

const paramsObject=params=>Object.fromEntries(params.entries())

export default function Products() {
  const [searchParams,setSearchParams]=useSearchParams()
  const {notify}=useAuth()
  const [data,setData]=useState({content:[],totalElements:0})
  const [categories,setCategories]=useState([])
  const [loading,setLoading]=useState(true)
  const [failed,setFailed]=useState(false)
  const [retry,setRetry]=useState(0)
  const [filters,setFilters]=useState(()=>paramsObject(searchParams))

  useEffect(()=>{api.get('/categories').then(setCategories)},[])
  useEffect(()=>setFilters(paramsObject(searchParams)),[searchParams])
  useEffect(()=>{
    let active=true
    setLoading(true)
    setFailed(false)
    api.get('/products',{params:paramsObject(searchParams)})
      .then(result=>{if(active)setData(result)})
      .catch(()=>{if(active)setFailed(true)})
      .finally(()=>{if(active)setLoading(false)})
    return()=>{active=false}
  },[searchParams,retry])

  const submit=event=>{
    event.preventDefault()
    const min=filters.minPrice===''?null:Number(filters.minPrice)
    const max=filters.maxPrice===''?null:Number(filters.maxPrice)
    if(min!==null&&max!==null&&min>max){notify('最低价不能高于最高价');return}
    const query={}
    Object.entries(filters).forEach(([key,value])=>{if(key!=='page'&&value!==''&&value!=null)query[key]=value})
    setSearchParams(query)
  }
  const reset=()=>{
    setFilters({})
    setSearchParams({})
  }
  const turnPage=page=>{
    setSearchParams({...paramsObject(searchParams),page})
    window.scrollTo({top:0,behavior:'smooth'})
  }

  return <div className="page wrap">
    <div className="page-title"><span>MARKETPLACE</span><h1>逛逛校园好物</h1><p>认真生活的人，也值得淘到恰到好处的好东西。</p></div>
    <form className="filter-panel" onSubmit={submit}>
      <label className="wide"><Search/><input aria-label="商品关键词" placeholder="搜索商品名称或描述" value={filters.keyword||''} onChange={event=>setFilters({...filters,keyword:event.target.value})}/></label>
      <select aria-label="商品分类" value={filters.categoryId||''} onChange={event=>setFilters({...filters,categoryId:event.target.value})}><option value="">全部分类</option>{categories.map(category=><option key={category.id} value={category.id}>{category.name}</option>)}</select>
      <input aria-label="最低价格" type="number" min="0" step="0.01" placeholder="最低价" value={filters.minPrice||''} onChange={event=>setFilters({...filters,minPrice:event.target.value})}/>
      <input aria-label="最高价格" type="number" min="0" step="0.01" placeholder="最高价" value={filters.maxPrice||''} onChange={event=>setFilters({...filters,maxPrice:event.target.value})}/>
      <select aria-label="排序方式" value={filters.sort||'newest'} onChange={event=>setFilters({...filters,sort:event.target.value})}><option value="newest">最新发布</option><option value="priceAsc">价格从低到高</option><option value="priceDesc">价格从高到低</option><option value="popular">浏览最多</option></select>
      <div className="filter-actions"><button className="btn filter-reset" type="button" onClick={reset} title="重置筛选"><RotateCcw size={17}/><span>重置</span></button><button className="btn primary" type="submit"><SlidersHorizontal size={17}/>筛选</button></div>
    </form>
    <div className="result-line" aria-live="polite"><b>{data.totalElements||0}</b> 件好物<span> · 每一件都来自真实校园生活</span></div>
    {loading?<div className="skeleton-grid" aria-label="正在加载商品">{Array.from({length:8},(_,index)=><div className="skeleton-card" key={index}><i/><span/><small/></div>)}</div>:failed?<div className="empty"><Search/><h3>商品加载失败</h3><p>请检查服务是否已启动，然后刷新页面重试。</p><button className="btn ghost" onClick={()=>setRetry(value=>value+1)}>重新加载</button></div>:data.content?.length?<>
      <div className="product-grid">{data.content.map(product=><ProductCard p={product} key={product.id}/>)}</div>
      {data.totalPages>1&&<div className="pagination"><button disabled={data.first} onClick={()=>turnPage(data.number-1)}>上一页</button><span>第 {data.number+1} / {data.totalPages} 页</span><button disabled={data.last} onClick={()=>turnPage(data.number+1)}>下一页</button></div>}
    </>:<div className="empty"><Search/><h3>暂时没找到合适的好物</h3><p>换个关键词或价格范围试试吧</p><button className="btn ghost" type="button" onClick={reset}>清除筛选</button><Link className="btn primary" to="/products/create">发布闲置</Link></div>}
  </div>
}
