import {Check,Clock3,MapPin,PackageCheck,Star,WalletCards} from 'lucide-react'
import {useEffect,useState} from 'react'
import {Link} from 'react-router-dom'
import {api,date,imageError,money,statusText} from '../api'
import {useAuth} from '../AuthContext'

const steps=['PENDING_CONFIRM','PENDING_PAYMENT','PENDING_DELIVERY','PENDING_RECEIVE','COMPLETED']

export default function Orders() {
  const {user,notify}=useAuth()
  const [orders,setOrders]=useState([])
  const [filter,setFilter]=useState('ALL')
  const [loading,setLoading]=useState(true)
  const [busyId,setBusyId]=useState(null)
  const [review,setReview]=useState(null)
  const [rating,setRating]=useState(5)
  const [content,setContent]=useState('')
  const load=()=>api.get('/orders').then(setOrders).finally(()=>setLoading(false))

  useEffect(()=>{load()},[])

  const action=async(order,actionName)=>{
    if(actionName==='pay'&&!confirm('这是课程模拟支付，不会产生真实扣款。确认继续？'))return
    setBusyId(order.id)
    try{
      await api.post(`/orders/${order.id}/${actionName}`)
      notify({confirm:'已确认订单',pay:'模拟支付成功',deliver:'已确认交付',receive:'交易完成',cancel:'订单已取消'}[actionName])
      await load()
    }finally{setBusyId(null)}
  }
  const openReview=order=>{setReview(order);setRating(5);setContent('')}
  const submitReview=async event=>{
    event.preventDefault()
    setBusyId(review.id)
    try{
      await api.post(`/orders/${review.id}/reviews`,{rating,content})
      setOrders(current=>current.map(order=>order.id===review.id?{...order,reviewed:true}:order))
      notify('评价发布成功')
      setReview(null)
    }finally{setBusyId(null)}
  }
  const visible=orders.filter(order=>filter==='ALL'||order.status===filter)

  return <div className="page wrap">
    <div className="page-title split"><div><span>MY ORDERS</span><h1>我的交易</h1><p>买入与卖出，都在这里清晰流转。</p></div><div className="order-count"><b>{orders.length}</b><span>笔交易记录</span></div></div>
    <div className="tabs" role="tablist" aria-label="订单状态筛选"><button className={filter==='ALL'?'active':''} onClick={()=>setFilter('ALL')}>全部</button>{steps.slice(0,4).map(status=><button className={filter===status?'active':''} onClick={()=>setFilter(status)} key={status}>{statusText[status]}</button>)}<button className={filter==='COMPLETED'?'active':''} onClick={()=>setFilter('COMPLETED')}>已完成</button></div>
    {loading?<div className="loader">正在加载交易记录…</div>:<div className="orders-list">{visible.map(order=>{
      const index=steps.indexOf(order.status)
      const isBuyer=order.buyer.id===user.id
      const busy=busyId===order.id
      return <article className="order-card" key={order.id}>
        <div className="order-head"><span>订单号 {order.orderNo}</span><time>{date(order.createdAt)}</time><b className={`status ${order.status}`}>{statusText[order.status]}</b></div>
        <div className="order-content">{order.product.imageUrls?.[0]?<img src={order.product.imageUrls[0]} onError={imageError} alt={order.product.title}/>:<div className="order-image-fallback">南小</div>}<div className="order-product"><h3>{order.product.title}</h3><p><MapPin/> {order.tradeLocation||'校内协商'}</p><span>{isBuyer?'卖家':'买家'}：{isBuyer?order.seller.nickname:order.buyer.nickname}</span></div><div className="order-price"><small>交易金额</small><b>¥{money(order.amount)}</b></div></div>
        {order.status!=='CANCELLED'&&<div className="order-steps">{steps.map((status,i)=><div className={(index>=i||order.status==='COMPLETED')?'done':''} key={status}><i>{index>i||order.status==='COMPLETED'?<Check/>:i+1}</i><span>{statusText[status]}</span></div>)}</div>}
        <div className="order-footer"><span>{order.buyerNote&&`备注：${order.buyerNote}`}</span><div>{['PENDING_CONFIRM','PENDING_PAYMENT'].includes(order.status)&&<button className="btn tiny ghost" disabled={busy} onClick={()=>action(order,'cancel')}>取消订单</button>}{order.status==='PENDING_CONFIRM'&&!isBuyer&&<button className="btn tiny primary" disabled={busy} onClick={()=>action(order,'confirm')}><Check/>确认订单</button>}{order.status==='PENDING_PAYMENT'&&isBuyer&&<button className="btn tiny primary" disabled={busy} onClick={()=>action(order,'pay')}><WalletCards/>模拟支付</button>}{order.status==='PENDING_DELIVERY'&&!isBuyer&&<button className="btn tiny primary" disabled={busy} onClick={()=>action(order,'deliver')}><PackageCheck/>确认已交付</button>}{order.status==='PENDING_RECEIVE'&&isBuyer&&<button className="btn tiny primary" disabled={busy} onClick={()=>action(order,'receive')}><Check/>确认收货</button>}{order.status==='COMPLETED'&&!order.reviewed&&<button className="btn tiny ghost" onClick={()=>openReview(order)}><Star/>评价交易</button>}{order.status==='COMPLETED'&&order.reviewed&&<span className="reviewed"><Check/>已评价</span>}{((order.status==='PENDING_CONFIRM'&&isBuyer)||(order.status==='PENDING_PAYMENT'&&!isBuyer)||(order.status==='PENDING_DELIVERY'&&isBuyer)||(order.status==='PENDING_RECEIVE'&&!isBuyer))&&<span className="waiting"><Clock3/>等待对方操作</span>}</div></div>
      </article>
    })}{!visible.length&&<div className="empty"><PackageCheck/><h3>这里还没有订单</h3><p>去集市看看，也许会遇到喜欢的好物。</p><Link className="btn primary" to="/products">逛逛好物</Link></div>}</div>}
    {review&&<div className="modal-mask" onClick={()=>busyId!==review.id&&setReview(null)}><form className="modal" onSubmit={submitReview} onClick={event=>event.stopPropagation()}><h2>评价这次交易</h2><p>你的真实反馈会帮助校园交易社区变得更好。</p><div className="rating" aria-label={`${rating} 星评分`}>{[1,2,3,4,5].map(number=><button type="button" aria-label={`${number} 星`} className={number<=rating?'active':''} onClick={()=>setRating(number)} key={number}><Star/></button>)}</div><label>评价内容<textarea maxLength="500" value={content} onChange={event=>setContent(event.target.value)} placeholder="交易顺利吗？说说你的感受"/><small className="field-count">{content.length}/500</small></label><div className="modal-actions"><button type="button" className="btn ghost" disabled={busyId===review.id} onClick={()=>setReview(null)}>取消</button><button className="btn primary" disabled={busyId===review.id}>{busyId===review.id?'发布中…':'发布评价'}</button></div></form></div>}
  </div>
}
