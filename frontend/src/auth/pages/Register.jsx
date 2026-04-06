import { useState, useEffect, useRef } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { register } from '../../shared/lib/api'
import { ArrowRight, ArrowLeft } from 'lucide-react'

const vertexSmokeySource = `
  attribute vec4 a_position;
  void main() {
    gl_Position = a_position;
  }
`

const fragmentSmokeySource = `
precision mediump float;
uniform vec2 iResolution;
uniform float iTime;
uniform vec2 iMouse;
uniform vec3 u_color;

void mainImage(out vec4 fragColor, in vec2 fragCoord){
    vec2 uv = fragCoord / iResolution;
    vec2 centeredUV = (2.0 * fragCoord - iResolution.xy) / min(iResolution.x, iResolution.y);
    float time = iTime * 0.5;
    vec2 mouse = iMouse / iResolution;
    vec2 rippleCenter = 2.0 * mouse - 1.0;
    vec2 distortion = centeredUV;
    for (float i = 1.0; i < 8.0; i++) {
        distortion.x += 0.5 / i * cos(i * 2.0 * distortion.y + time + rippleCenter.x * 3.1415);
        distortion.y += 0.5 / i * cos(i * 2.0 * distortion.x + time + rippleCenter.y * 3.1415);
    }
    float wave = abs(sin(distortion.x + distortion.y + time));
    float glow = smoothstep(0.9, 0.2, wave);
    fragColor = vec4(u_color * glow, 1.0);
}

void main() {
    mainImage(gl_FragColor, gl_FragCoord.xy);
}
`

function SmokeyBackground({ color = '#4338ca' }) {
  const canvasRef = useRef(null)
  const mousePos = useRef({ x: 0, y: 0 })
  const isHovering = useRef(false)

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return
    const gl = canvas.getContext('webgl')
    if (!gl) return

    const compileShader = (type, source) => {
      const shader = gl.createShader(type)
      gl.shaderSource(shader, source)
      gl.compileShader(shader)
      if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) return null
      return shader
    }

    const vertexShader = compileShader(gl.VERTEX_SHADER, vertexSmokeySource)
    const fragmentShader = compileShader(gl.FRAGMENT_SHADER, fragmentSmokeySource)
    if (!vertexShader || !fragmentShader) return

    const program = gl.createProgram()
    gl.attachShader(program, vertexShader)
    gl.attachShader(program, fragmentShader)
    gl.linkProgram(program)
    gl.useProgram(program)

    const positionBuffer = gl.createBuffer()
    gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer)
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1, -1, 1, -1, -1, 1, -1, 1, 1, -1, 1, 1]), gl.STATIC_DRAW)

    const positionLocation = gl.getAttribLocation(program, 'a_position')
    gl.enableVertexAttribArray(positionLocation)
    gl.vertexAttribPointer(positionLocation, 2, gl.FLOAT, false, 0, 0)

    const iResolutionLoc = gl.getUniformLocation(program, 'iResolution')
    const iTimeLoc = gl.getUniformLocation(program, 'iTime')
    const iMouseLoc = gl.getUniformLocation(program, 'iMouse')
    const uColorLoc = gl.getUniformLocation(program, 'u_color')

    const r = parseInt(color.substring(1, 3), 16) / 255
    const g = parseInt(color.substring(3, 5), 16) / 255
    const b = parseInt(color.substring(5, 7), 16) / 255
    gl.uniform3f(uColorLoc, r, g, b)

    const startTime = Date.now()
    let animId

    const render = () => {
      const w = canvas.clientWidth
      const h = canvas.clientHeight
      canvas.width = w
      canvas.height = h
      gl.viewport(0, 0, w, h)
      const t = (Date.now() - startTime) / 1000
      gl.uniform2f(iResolutionLoc, w, h)
      gl.uniform1f(iTimeLoc, t)
      gl.uniform2f(iMouseLoc,
        isHovering.current ? mousePos.current.x : w / 2,
        isHovering.current ? h - mousePos.current.y : h / 2
      )
      gl.drawArrays(gl.TRIANGLES, 0, 6)
      animId = requestAnimationFrame(render)
    }

    const onMove = (e) => {
      const rect = canvas.getBoundingClientRect()
      mousePos.current = { x: e.clientX - rect.left, y: e.clientY - rect.top }
    }
    canvas.addEventListener('mousemove', onMove)
    canvas.addEventListener('mouseenter', () => { isHovering.current = true })
    canvas.addEventListener('mouseleave', () => { isHovering.current = false })

    render()
    return () => {
      cancelAnimationFrame(animId)
      canvas.removeEventListener('mousemove', onMove)
    }
  }, [color])

  return (
    <div className="absolute inset-0 w-full h-full overflow-hidden">
      <canvas ref={canvasRef} className="w-full h-full" />
      <div className="absolute inset-0 backdrop-blur-sm" />
    </div>
  )
}

function RegisterForm({ role, onBack }) {
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '', adminCode: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const isConsultant = role === 'CONSULTANT'
  const isAdmin = role === 'ADMIN'

  const handleChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register({ ...form, role })
      navigate('/login', {
        state: {
          message: isConsultant
            ? 'Application submitted! Please log in — your account will be active once an admin approves it.'
            : 'Account created! Please log in.',
        },
      })
    } catch (err) {
      const data = err.response?.data
      if (data?.errors?.length) {
        setError(data.errors.map((e) => `${e.field}: ${e.message}`).join(', '))
      } else {
        setError(data?.message ?? data?.error ?? 'Registration failed')
      }
    } finally {
      setLoading(false)
    }
  }

  const badge = isAdmin
    ? 'bg-red-500/20 text-red-400'
    : isConsultant
    ? 'bg-indigo-500/20 text-indigo-400'
    : 'bg-green-500/20 text-green-400'

  const badgeLabel = isAdmin ? 'Admin Account' : isConsultant ? 'Consultant Application' : 'Client Account'

  const inputCls = 'block py-2.5 px-0 w-full text-sm text-white bg-transparent border-0 border-b-2 border-gray-300 appearance-none focus:outline-none focus:ring-0 focus:border-indigo-500 peer'
  const labelCls = 'absolute text-sm text-gray-300 duration-300 transform -translate-y-6 scale-75 top-3 -z-10 origin-[0] peer-focus:text-indigo-400 peer-placeholder-shown:scale-100 peer-placeholder-shown:translate-y-0 peer-focus:scale-75 peer-focus:-translate-y-6'

  return (
    <div className="relative z-10 w-full max-w-sm p-8 space-y-5 bg-white/10 backdrop-blur-lg rounded-2xl border border-white/20 shadow-2xl">
      <button onClick={onBack} className="flex items-center gap-1 text-xs text-gray-400 hover:text-white transition-colors">
        <ArrowLeft size={14} /> Back
      </button>

      <div>
        <div className={`inline-flex items-center text-xs font-medium px-2.5 py-1 rounded-full mb-3 ${badge}`}>
          {badgeLabel}
        </div>
        <h2 className="text-3xl font-bold text-white">Create Account</h2>
        {isConsultant && <p className="mt-1 text-sm text-gray-300">Your application will be reviewed by an admin before you can accept bookings.</p>}
        {isAdmin && <p className="mt-1 text-sm text-gray-300">Admin registration requires a secret code.</p>}
      </div>

      {error && (
        <div className="text-sm text-red-400 bg-red-500/10 border border-red-500/30 rounded-lg px-3 py-2">{error}</div>
      )}

      <form onSubmit={handleSubmit} className="space-y-7">
        <div className="grid grid-cols-2 gap-6">
          <div className="relative z-0">
            <input name="firstName" required value={form.firstName} onChange={handleChange} placeholder=" " className={inputCls} />
            <label className={labelCls}>First name</label>
          </div>
          <div className="relative z-0">
            <input name="lastName" required value={form.lastName} onChange={handleChange} placeholder=" " className={inputCls} />
            <label className={labelCls}>Last name</label>
          </div>
        </div>

        <div className="relative z-0">
          <input type="email" name="email" required value={form.email} onChange={handleChange} placeholder=" " className={inputCls} />
          <label className={labelCls}>Email Address</label>
        </div>

        <div className="relative z-0">
          <input type="password" name="password" required value={form.password} onChange={handleChange} placeholder=" " className={inputCls} />
          <label className={labelCls}>Password</label>
        </div>

        {isAdmin && (
          <div className="relative z-0">
            <input type="password" name="adminCode" required value={form.adminCode} onChange={handleChange} placeholder=" " className={inputCls} />
            <label className={labelCls}>Admin Code</label>
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="group w-full flex items-center justify-center py-3 px-4 bg-indigo-600 hover:bg-indigo-700 rounded-lg text-white font-semibold transition-all duration-300 disabled:opacity-50"
        >
          {loading ? 'Creating…' : isConsultant ? 'Submit Application' : 'Create Account'}
          {!loading && <ArrowRight className="ml-2 h-5 w-5 transform group-hover:translate-x-1 transition-transform" />}
        </button>
      </form>

      <p className="text-center text-xs text-gray-400">
        Already have an account?{' '}
        <Link to="/login" className="font-semibold text-indigo-400 hover:text-indigo-300 transition">Sign in</Link>
      </p>
    </div>
  )
}

export default function Register() {
  const [role, setRole] = useState(null)

  const roleCards = [
    {
      role: 'CLIENT',
      label: "I'm a Client",
      desc: 'Browse services, book sessions, and manage payments.',
      cta: 'Get started →',
      color: 'green',
      icon: <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />,
    },
    {
      role: 'CONSULTANT',
      label: "I'm a Consultant",
      desc: 'Offer expertise, set availability, and manage bookings.',
      cta: 'Apply now →',
      sub: 'Requires admin approval',
      color: 'indigo',
      icon: <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />,
    },
    {
      role: 'ADMIN',
      label: "I'm an Admin",
      desc: 'Manage consultants, services, and platform policies.',
      cta: 'Register →',
      sub: 'Requires admin code',
      color: 'red',
      icon: <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />,
    },
  ]

  const colorMap = {
    green: { icon: 'text-green-400', iconBg: 'bg-green-500/10 group-hover:bg-green-500/20', border: 'hover:border-green-500/50', cta: 'text-green-400 group-hover:text-green-300' },
    indigo: { icon: 'text-indigo-400', iconBg: 'bg-indigo-500/10 group-hover:bg-indigo-500/20', border: 'hover:border-indigo-500/50', cta: 'text-indigo-400 group-hover:text-indigo-300' },
    red: { icon: 'text-red-400', iconBg: 'bg-red-500/10 group-hover:bg-red-500/20', border: 'hover:border-red-500/50', cta: 'text-red-400 group-hover:text-red-300' },
  }

  return (
    <div className="relative min-h-screen flex items-center justify-center bg-[#16171d] overflow-hidden p-4">
      <SmokeyBackground color="#4338ca" />

      {role ? (
        <RegisterForm role={role} onBack={() => setRole(null)} />
      ) : (
        <div className="relative z-10 w-full max-w-2xl">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-white">Join ConsultHub</h1>
            <p className="text-gray-300 mt-2">How would you like to use the platform?</p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            {roleCards.map(({ role: r, label, desc, cta, sub, color, icon }) => {
              const c = colorMap[color]
              return (
                <button
                  key={r}
                  onClick={() => setRole(r)}
                  className={`bg-white/10 backdrop-blur-lg hover:bg-white/15 border border-white/20 ${c.border} rounded-2xl p-6 text-left transition-all group`}
                >
                  <div className={`w-12 h-12 rounded-xl flex items-center justify-center mb-4 transition-colors ${c.iconBg}`}>
                    <svg className={`w-6 h-6 ${c.icon}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">{icon}</svg>
                  </div>
                  <h2 className="font-semibold text-white text-lg mb-1">{label}</h2>
                  <p className="text-sm text-gray-300">{desc}</p>
                  <div className="mt-4 flex flex-col gap-0.5">
                    <span className={`text-sm font-medium ${c.cta}`}>{cta}</span>
                    {sub && <span className="text-xs text-gray-500">{sub}</span>}
                  </div>
                </button>
              )
            })}
          </div>

          <p className="mt-6 text-center text-sm text-gray-400">
            Already have an account?{' '}
            <Link to="/login" className="font-semibold text-indigo-400 hover:text-indigo-300 transition">Sign in</Link>
          </p>
        </div>
      )}
    </div>
  )
}
