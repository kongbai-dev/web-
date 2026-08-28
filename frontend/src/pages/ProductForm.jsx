import {ArrowLeft, ImagePlus, LoaderCircle, Save, Upload} from 'lucide-react'
import {useEffect, useState} from 'react'
import {Link, useNavigate, useParams} from 'react-router-dom'
import {api, imageError} from '../api'
import {useAuth} from '../AuthContext'

const initialForm = {title:'', description:'', categoryId:'', originalPrice:'', price:'', conditionLevel:'九成新', tradeType:'SALE', tradeLocation:'', imageUrl:''}

export default function ProductForm() {
  const {id} = useParams()
  const navigate = useNavigate()
  const {notify} = useAuth()
  const [categories, setCategories] = useState([])
  const [busy, setBusy] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [form, setForm] = useState(initialForm)

  useEffect(() => {
    api.get('/categories').then(setCategories)
    if (id) api.get(`/products/${id}`).then(product => setForm({...product, categoryId:product.category.id, imageUrl:product.imageUrls?.[0] || ''}))
  }, [id])

  const change = event => setForm({...form, [event.target.name]:event.target.value})
  const upload = async event => {
    const file = event.target.files?.[0]
    if (!file) return
    setUploading(true)
    try {
      const body = new FormData()
      body.append('file', file)
      const result = await api.post('/uploads/images', body)
      setForm(current => ({...current, imageUrl:result.url}))
      notify('图片上传成功')
    } finally {
      setUploading(false)
      event.target.value = ''
    }
  }
  const submit = async event => {
    event.preventDefault()
    setBusy(true)
    try {
      const body = {...form, imageUrls:form.imageUrl ? [form.imageUrl] : []}
      const product = id ? await api.put(`/products/${id}`, body) : await api.post('/products', body)
      notify(id ? '商品信息已更新' : '发布成功，去看看你的好物吧')
      navigate(`/products/${product.id}`)
    } finally { setBusy(false) }
  }

  return <div className="page wrap form-page">
    <Link className="back" to={id ? `/products/${id}` : '/products'}><ArrowLeft/>返回</Link>
    <div className="form-heading"><span>{id ? 'EDIT ITEM' : 'NEW LISTING'}</span><h1>{id ? '编辑闲置' : '发布一件闲置'}</h1><p>认真描述物品，会让交易来得更快一点。</p></div>
    <form className="product-form" onSubmit={submit}>
      <section>
        <h3><i>1</i>基本信息</h3>
        <label className="full"><span>商品标题 *</span><input name="title" value={form.title} onChange={change} maxLength="100" required placeholder="简洁清楚，例如：九成新 iPad Air 5"/></label>
        <label className="full"><span>详细描述</span><textarea name="description" value={form.description} onChange={change} rows="6" maxLength="5000" placeholder="使用时长、瑕疵情况、配件等，描述越真实越容易成交"/></label>
        <div className="form-grid">
          <label><span>分类 *</span><select name="categoryId" value={form.categoryId} onChange={change} required><option value="">请选择</option>{categories.map(c=><option value={c.id} key={c.id}>{c.name}</option>)}</select></label>
          <label><span>成色</span><select name="conditionLevel" value={form.conditionLevel} onChange={change}><option>全新</option><option>九成新</option><option>八成新</option><option>七成新</option><option>有明显使用痕迹</option></select></label>
          <label><span>流转方式</span><select name="tradeType" value={form.tradeType} onChange={change}><option value="SALE">出售</option><option value="GIFT">赠送</option><option value="EXCHANGE">交换</option></select></label>
          <label><span>交易地点</span><input name="tradeLocation" value={form.tradeLocation} onChange={change} maxLength="100" placeholder="如：图书馆南门"/></label>
        </div>
      </section>
      <section>
        <h3><i>2</i>价格与图片</h3>
        <div className="form-grid">
          <label><span>出售价格 *</span><input type="number" min="0" step="0.01" name="price" value={form.price} onChange={change} required placeholder="¥ 0.00"/></label>
          <label><span>购入原价</span><input type="number" min="0" step="0.01" name="originalPrice" value={form.originalPrice} onChange={change} placeholder="选填"/></label>
        </div>
        <label className="full"><span>商品图片 URL</span><div className="image-url"><ImagePlus/><input name="imageUrl" value={form.imageUrl} onChange={change} placeholder="粘贴图片地址，或使用下方本地上传"/></div></label>
        <div className="upload-row">
          <label className={`upload-button ${uploading ? 'disabled' : ''}`}>{uploading ? <LoaderCircle className="spin"/> : <Upload/>}{uploading ? '正在上传…' : '从本地选择图片'}<input type="file" accept="image/jpeg,image/png,image/webp,image/gif" onChange={upload} disabled={uploading}/></label>
          <small>支持 JPG、PNG、WebP、GIF，最大 5MB</small>
        </div>
        {form.imageUrl && <img className="image-preview" src={form.imageUrl} onError={imageError} alt="商品预览"/>}
      </section>
      <div className="form-submit"><span>发布即代表你同意遵守校园交易规范</span><button className="btn primary" disabled={busy || uploading}><Save/> {busy ? '保存中…' : id ? '保存修改' : '确认发布'}</button></div>
    </form>
  </div>
}
