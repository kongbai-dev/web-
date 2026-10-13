import {Clock3,Edit3,Heart,Package,Plus,Star,UserRound} from 'lucide-react'
import {useEffect,useState} from 'react'
import {Link} from 'react-router-dom'
import {api,statusText} from '../api'
import {useAuth} from '../AuthContext'
import ProductCard from '../components/ProductCard'

const tabs=[['profile','/profile',UserRound,'个人资料'],['products','/my-products',Package,'我的发布'],['favorites','/favorites',Heart,'我的收藏'],['history','/history',Clock3,'浏览记录']]

export default function Profile({tab='profile'}) {
  const {user,refresh,notify}=useAuth()
  const [items,setItems]=useState([])
  const [form,setForm]=useState(user)
  const [loading,setLoading]=useState(tab!=='profile')
  const [busy,setBusy]=useState(false)

  useEffect(()=>setForm(user),[user])
  useEffect(()=>{
    if(tab==='profile'){setLoading(false);return}
    let active=true
    const path={favorites:'/users/me/favorites',history:'/users/me/history',products:'/users/me/products'}[tab]
    setLoading(true)
    api.get(path).then(result=>{if(active)setItems(result.content||result)}).finally(()=>{if(active)setLoading(false)})
    return()=>{active=false}
  },[tab])

  const save=async event=>{
    event.preventDefault()
    setBusy(true)
    try{
      const {nickname,phone,campus,dormArea,bio}=form
      await api.put('/users/me',{nickname,phone,campus,dormArea,bio})
      await refresh()
      notify('个人资料已保存')
    }finally{setBusy(false)}
  }
  const manage=async(product,action)=>{
    setBusy(true)
    try{
      await api.post(`/products/${product.id}/${action}`)
      const status=action==='relist'?'ON_SALE':'OFF_SHELF'
      setItems(current=>current.map(item=>item.id===product.id?{...item,status}:item))
      notify(action==='relist'?'商品已重新上架':'商品已下架')
    }finally{setBusy(false)}
  }

  return <div className="page wrap profile-layout">
    <aside className="profile-side"><div className="profile-avatar">{user.nickname?.[0]}</div><h2>{user.nickname}</h2><p>@{user.username}</p><div className="credit"><Star/> <b>{user.creditScore?.toFixed(1)||'5.0'}</b><span>信用评分 · {user.reviewCount} 条评价</span></div><nav>{tabs.map(([key,path,Icon,label])=><Link className={tab===key?'active':''} to={path} key={key}><Icon/>{label}</Link>)}</nav></aside>
    <section className="profile-main">{tab==='profile'?<>
      <div className="content-head"><div><span>PROFILE</span><h1>个人资料</h1></div><Edit3/></div>
      <form className="profile-form" onSubmit={save}><div className="form-grid"><label><span>昵称</span><input required maxLength="50" value={form.nickname||''} onChange={event=>setForm({...form,nickname:event.target.value})}/></label><label><span>学号</span><input value={form.studentNo||''} disabled/></label><label><span>手机号</span><input maxLength="30" value={form.phone||''} onChange={event=>setForm({...form,phone:event.target.value})}/></label><label><span>注册邮箱</span><input type="email" value={form.email||'未绑定'} disabled title="注册邮箱已通过验证，不支持直接修改"/></label><label><span>校区</span><input maxLength="100" value={form.campus||''} onChange={event=>setForm({...form,campus:event.target.value})}/></label><label><span>宿舍区域</span><input maxLength="100" value={form.dormArea||''} onChange={event=>setForm({...form,dormArea:event.target.value})}/></label></div><label><span>个人简介</span><textarea maxLength="255" value={form.bio||''} onChange={event=>setForm({...form,bio:event.target.value})}/><small className="field-count">{(form.bio||'').length}/255</small></label><button className="btn primary" disabled={busy}>{busy?'保存中…':'保存资料'}</button></form>
    </>:<>
      <div className="content-head"><div><span>{tab.toUpperCase()}</span><h1>{tabs.find(item=>item[0]===tab)?.[3]}</h1></div>{tab==='products'?<Link className="btn tiny primary" to="/products/create"><Plus/>发布闲置</Link>:<b>{items.length} 件</b>}</div>
      {loading?<div className="loader">正在加载…</div>:items.length?<div className="product-grid compact">{items.map(product=><div className="mine-wrap" key={product.id}><ProductCard p={product}/>{tab==='products'&&<div className="mine-actions"><span className={`status ${product.status}`}>{statusText[product.status]}</span><div>{['ON_SALE','OFF_SHELF'].includes(product.status)&&<Link to={`/products/${product.id}/edit`}>编辑</Link>}{product.status==='ON_SALE'&&<button disabled={busy} onClick={()=>manage(product,'off-shelf')}>下架</button>}{product.status==='OFF_SHELF'&&<button disabled={busy} onClick={()=>manage(product,'relist')}>上架</button>}</div></div>}</div>)}</div>:<div className="empty"><Heart/><h3>暂时空空如也</h3><p>{tab==='products'?'发布第一件闲置，让好物继续流转。':'去逛逛校园好物吧。'}</p><Link className="btn primary" to={tab==='products'?'/products/create':'/products'}>{tab==='products'?'发布闲置':'逛逛好物'}</Link></div>}
    </>}</section>
  </div>
}
