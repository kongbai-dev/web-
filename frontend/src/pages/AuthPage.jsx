import {ArrowLeft,Leaf,LockKeyhole,Mail,ShieldCheck,UserRound} from 'lucide-react'
import {useEffect,useState} from 'react'
import {Link,useLocation,useNavigate} from 'react-router-dom'
import {api} from '../api'
import {useAuth} from '../AuthContext'

const initialForm=register=>({username:register?'':'student',password:register?'':'123456',studentNo:'',nickname:'',email:'',emailCode:''})

export default function AuthPage({register=false}) {
  const [form,setForm]=useState(()=>initialForm(register))
  const [busy,setBusy]=useState(false)
  const [codeBusy,setCodeBusy]=useState(false)
  const [cooldown,setCooldown]=useState(0)
  const {login,notify}=useAuth()
  const navigate=useNavigate()
  const location=useLocation()

  useEffect(()=>{setForm(initialForm(register));setCooldown(0)},[register])
  useEffect(()=>{if(cooldown<=0)return;const timer=setTimeout(()=>setCooldown(value=>Math.max(0,value-1)),1000);return()=>clearTimeout(timer)},[cooldown])

  const change=event=>setForm({...form,[event.target.name]:event.target.value})
  const sendCode=async()=>{
    const email=form.email.trim()
    if(!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)){notify('请输入正确的邮箱地址');return}
    setCodeBusy(true)
    try{
      const result=await api.post('/auth/email-code',{email})
      setCooldown(result.retryAfterSeconds||60)
      notify('验证码已发送，请查看邮箱')
    }finally{setCodeBusy(false)}
  }
  const submit=async event=>{
    event.preventDefault()
    setBusy(true)
    try{
      const data=await api.post(register?'/auth/register':'/auth/login',form)
      login(data)
      navigate(location.state?.from||'/')
    }finally{setBusy(false)}
  }

  return <div className="auth-page">
    <div className="auth-visual"><Link to="/"><ArrowLeft/>返回首页</Link><div><span className="eyebrow"><Leaf/>循环，让校园更轻盈</span><h1>从一次闲置流转，<br/>认识校园的另一面。</h1><p>真实、友善、近在身边。</p></div><small>南小集 CAMPUS REUSE MARKET</small></div>
    <div className="auth-form"><form onSubmit={submit}><div className="auth-logo"><span className="brand-mark">南</span></div><h2>{register?'加入南小集':'欢迎回来'}</h2><p>{register?'使用邮箱验证并创建校园账号':'登录后继续发现身边的好物'}</p><label><span>{register?'用户名':'用户名或邮箱'}</span><div><UserRound/><input name="username" autoComplete="username" maxLength="100" value={form.username} onChange={change} required placeholder={register?'请输入用户名':'请输入用户名或邮箱'}/></div></label>{register&&<><label><span>邮箱</span><div><Mail/><input type="email" name="email" autoComplete="email" maxLength="100" value={form.email} onChange={change} required placeholder="用于接收注册验证码"/></div></label><label><span>邮箱验证码</span><div className="verification-input"><ShieldCheck/><input name="emailCode" inputMode="numeric" autoComplete="one-time-code" pattern="[0-9]{6}" maxLength="6" value={form.emailCode} onChange={change} required placeholder="6 位验证码"/><button type="button" disabled={codeBusy||cooldown>0} onClick={sendCode}>{codeBusy?'发送中…':cooldown>0?`${cooldown}s 后重发`:'发送验证码'}</button></div></label><label><span>学号</span><input name="studentNo" autoComplete="off" maxLength="30" value={form.studentNo} onChange={change} required placeholder="请输入学号"/></label><label><span>昵称</span><input name="nickname" maxLength="50" value={form.nickname} onChange={change} required placeholder="大家怎么称呼你"/></label></>}<label><span>密码</span><div><LockKeyhole/><input type="password" name="password" autoComplete={register?'new-password':'current-password'} value={form.password} onChange={change} minLength="6" maxLength="72" required placeholder="至少 6 位字符"/></div></label><button className="btn primary full" disabled={busy}>{busy?'请稍候…':register?'验证并注册':'登录'}</button><div className="auth-switch">{register?'已有账号？':'还没有账号？'} <Link to={register?'/login':'/register'}>{register?'直接登录':'立即注册'}</Link></div>{!register&&<div className="demo-box"><b>演示账号</b><span>学生 student / 123456</span><span>卖家 seller / 123456</span><span>管理员 admin / 123456</span></div>}</form></div>
  </div>
}
