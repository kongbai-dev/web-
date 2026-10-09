import {createContext,useContext,useEffect,useState} from 'react'
import {api} from './api'
const C=createContext(null)
const storedUser=()=>{try{return localStorage.getItem('token')?JSON.parse(localStorage.getItem('user')||'null'):null}catch{return null}}
export function AuthProvider({children}){const [user,setUser]=useState(storedUser);const [toast,setToast]=useState('')
 useEffect(()=>{let timer;const f=e=>{setToast(e.detail);clearTimeout(timer);timer=setTimeout(()=>setToast(''),2600)};const expired=()=>setUser(null);const storage=e=>{if(e.key==='token'||e.key==='user')setUser(storedUser())};window.addEventListener('app-error',f);window.addEventListener('auth-expired',expired);window.addEventListener('storage',storage);return()=>{clearTimeout(timer);window.removeEventListener('app-error',f);window.removeEventListener('auth-expired',expired);window.removeEventListener('storage',storage)}},[])
 const login=data=>{localStorage.setItem('token',data.token);localStorage.setItem('user',JSON.stringify(data.user));setUser(data.user)}
 const logout=()=>{localStorage.removeItem('token');localStorage.removeItem('user');setUser(null)}
 const refresh=async()=>{try{const u=await api.get('/users/me');localStorage.setItem('user',JSON.stringify(u));setUser(u)}catch{} }
 return <C.Provider value={{user,login,logout,refresh,notify:m=>window.dispatchEvent(new CustomEvent('app-error',{detail:m}))}}>{children}{toast&&<div className="toast" role="status" aria-live="polite">{toast}</div>}</C.Provider>}
export const useAuth=()=>useContext(C)
