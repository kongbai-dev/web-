import {Heart,LayoutDashboard,LogOut,Menu,Package,Search,ShoppingBag,UserRound,X} from 'lucide-react'
import {Link,NavLink,useLocation,useNavigate} from 'react-router-dom'
import {useEffect,useState} from 'react'
import {useAuth} from '../AuthContext'

export default function Layout({children}) {
  const {user,logout}=useAuth()
  const navigate=useNavigate()
  const location=useLocation()
  const [open,setOpen]=useState(false)
  const [query,setQuery]=useState('')

  useEffect(()=>setOpen(false),[location.pathname,location.search])

  const search=event=>{
    event.preventDefault()
    const keyword=query.trim()
    navigate(keyword?`/products?keyword=${encodeURIComponent(keyword)}`:'/products')
    setOpen(false)
  }
  const signOut=()=>{
    logout()
    setOpen(false)
    navigate('/')
  }

  return <>
    <a className="skip-link" href="#main-content">跳到主要内容</a>
    <header>
      <div className="nav wrap">
        <Link className="brand" to="/" aria-label="南小集首页"><span className="brand-mark">南</span><span>南小集<small>校园闲置循环站</small></span></Link>
        <form className="nav-search" onSubmit={search} role="search"><Search size={18}/><input value={query} onChange={event=>setQuery(event.target.value)} aria-label="搜索商品" placeholder="搜索教材、数码、生活好物"/></form>
        <button className="mobile-menu" type="button" aria-label={open?'关闭导航':'打开导航'} aria-expanded={open} aria-controls="primary-navigation" onClick={()=>setOpen(value=>!value)}>{open?<X/>:<Menu/>}</button>
        <nav id="primary-navigation" className={open?'open':''} aria-label="主导航">
          <NavLink to="/products"><ShoppingBag size={17}/>逛闲置</NavLink>
          {user&&<>
            <NavLink to="/favorites"><Heart size={17}/>收藏</NavLink>
            <NavLink to="/orders"><Package size={17}/>订单</NavLink>
            {user.role==='ADMIN'&&<NavLink to="/admin"><LayoutDashboard size={17}/>管理台</NavLink>}
            <NavLink to="/profile"><UserRound size={17}/>{user.nickname}</NavLink>
            <button className="link-btn" type="button" aria-label="退出登录" title="退出登录" onClick={signOut}><LogOut size={17}/></button>
          </>}
          {!user&&<Link className="login-pill" to="/login">登录 / 注册</Link>}
          <Link className="publish" to={user?'/products/create':'/login'}>发布闲置</Link>
        </nav>
      </div>
    </header>
    <main id="main-content">{children}</main>
    <footer><div className="wrap footer-grid"><div><div className="brand footer-brand"><span className="brand-mark">南</span><span>南小集</span></div><p>让每一件闲置，在校园里重新发光。</p></div><div><b>安全交易</b><span>校内面交 · 状态透明 · 举报治理</span></div><div><b>课程项目</b><span>Spring Boot + React 前后端分离</span></div></div><div className="copyright">© 2026 南小集 · Campus Reuse Market</div></footer>
  </>
}
