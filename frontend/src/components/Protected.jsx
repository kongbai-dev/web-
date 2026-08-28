import {Navigate,useLocation} from 'react-router-dom'
import {useAuth} from '../AuthContext'
export default function Protected({children,admin=false}){const {user}=useAuth();const l=useLocation();if(!user)return <Navigate to="/login" state={{from:l.pathname}}/>;if(admin&&user.role!=='ADMIN')return <Navigate to="/"/>;return children}
