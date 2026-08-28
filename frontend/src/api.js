import axios from 'axios'
export const api=axios.create({baseURL:'/api'})
api.interceptors.request.use(c=>{const t=localStorage.getItem('token');if(t)c.headers.Authorization=`Bearer ${t}`;return c})
api.interceptors.response.use(r=>r.data.data,e=>{const m=e.response?.data?.message||'网络请求失败';window.dispatchEvent(new CustomEvent('app-error',{detail:m}));if(e.response?.status===401){localStorage.removeItem('token');localStorage.removeItem('user');window.dispatchEvent(new Event('auth-expired'))}return Promise.reject(new Error(m))})
export const money=n=>Number(n||0).toLocaleString('zh-CN',{minimumFractionDigits:0,maximumFractionDigits:2})
export const date=s=>s?new Date(s).toLocaleString('zh-CN',{month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit'}):'-'
export const statusText={ON_SALE:'在售',RESERVED:'已预订',SOLD:'已售出',OFF_SHELF:'已下架',PENDING_CONFIRM:'待卖家确认',PENDING_PAYMENT:'待买家付款',PENDING_DELIVERY:'待卖家交付',PENDING_RECEIVE:'待买家收货',COMPLETED:'已完成',CANCELLED:'已取消',PENDING:'待处理',APPROVED:'举报成立',REJECTED:'已驳回',ACTIVE:'正常',BANNED:'已封禁'}
export const placeholderImage=`data:image/svg+xml;charset=utf-8,${encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="800" height="600"><rect width="100%" height="100%" fill="#e5f3ed"/><text x="50%" y="50%" text-anchor="middle" dominant-baseline="middle" fill="#155e54" font-family="sans-serif" font-size="42">青禾好物</text></svg>')}`
export const imageError=e=>{if(e.currentTarget.src!==placeholderImage)e.currentTarget.src=placeholderImage}
