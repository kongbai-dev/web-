import {ArrowLeft,Leaf,LockKeyhole,UserRound} from 'lucide-react'
import {useEffect,useState} from 'react'
import {Link,useLocation,useNavigate} from 'react-router-dom'
import {api} from '../api'
import {useAuth} from '../AuthContext'

const initialForm=register=>({username:register?'':'student',password:register?'':'123456',studentNo:'',nickname:''})

export default function AuthPage({register=false}) {
  const [form,setForm]=useState(()=>initialForm(register))
  const [busy,setBusy]=useState(false)
  const {login}=useAuth()
  const navigate=useNavigate()
  const location=useLocation()

  useEffect(()=>setForm(initialForm(register)),[register])

  const change=event=>setForm({...form,[event.target.name]:event.target.value})
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
    <div className="auth-form"><form onSubmit={submit}><div className="auth-logo"><span className="brand-mark">南</span></div><h2>{register?'加入南小集':'欢迎回来'}</h2><p>{register?'创建你的校园循环账号':'登录后继续发现身边的好物'}</p><label><span>用户名</span><div><UserRound/><input name="username" autoComplete="username" maxLength="50" value={form.username} onChange={change} required placeholder="请输入用户名"/></div></label>{register&&<><label><span>学号</span><input name="studentNo" autoComplete="off" maxLength="30" value={form.studentNo} onChange={change} required placeholder="请输入学号"/></label><label><span>昵称</span><input name="nickname" maxLength="50" value={form.nickname} onChange={change} required placeholder="大家怎么称呼你"/></label></>}<label><span>密码</span><div><LockKeyhole/><input type="password" name="password" autoComplete={register?'new-password':'current-password'} value={form.password} onChange={change} minLength="6" maxLength="72" required placeholder="至少 6 位字符"/></div></label><button className="btn primary full" disabled={busy}>{busy?'请稍候…':register?'注册并登录':'登录'}</button><div className="auth-switch">{register?'已有账号？':'还没有账号？'} <Link to={register?'/login':'/register'}>{register?'直接登录':'立即注册'}</Link></div>{!register&&<div className="demo-box"><b>演示账号</b><span>学生 student / 123456</span><span>卖家 seller / 123456</span><span>管理员 admin / 123456</span></div>}</form></div>
  </div>
}
