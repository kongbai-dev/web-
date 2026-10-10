import {Heart,MapPin} from 'lucide-react'
import {Link} from 'react-router-dom'
import {imageError,money,statusText} from '../api'

export default function ProductCard({p}) {
  const status=p.status&&p.status!=='ON_SALE'?statusText[p.status]:null
  return <Link className="product-card" to={`/products/${p.id}`} aria-label={`${p.title}，${money(p.price)} 元`}>
    <div className="card-image">
      {p.imageUrls?.[0]?<img src={p.imageUrls[0]} onError={imageError} alt={p.title} loading="lazy"/>:<div className="image-fallback">南小集好物</div>}
      <span className="trade-tag">{p.tradeType==='GIFT'?'赠送':p.tradeType==='EXCHANGE'?'交换':'出售'}</span>
      {status&&<span className={`card-status status ${p.status}`}>{status}</span>}
    </div>
    <div className="card-body"><h3>{p.title}</h3><div className="price"><small>¥</small>{money(p.price)}{p.originalPrice&&<del>¥{money(p.originalPrice)}</del>}</div><div className="card-meta"><span>{p.conditionLevel||'成色未填写'}</span><span><MapPin size={13}/>{p.tradeLocation||'校内协商'}</span></div><div className="card-bottom"><span>{p.seller?.nickname}</span><span><Heart size={14}/>{p.favoriteCount||0}</span></div></div>
  </Link>
}
