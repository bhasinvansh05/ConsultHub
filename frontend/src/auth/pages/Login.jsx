import { useState, useEffect, useRef } from 'react'
import { useNavigate, Link, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { User, Lock, ArrowRight } from 'lucide-react'

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

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const successMessage = location.state?.message

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const decoded = await login(email, password)
      const r = Array.isArray(decoded?.roles)
        ? decoded.roles
        : (decoded?.roles ?? '').split(' ')
      const role = r[0]?.toUpperCase().replace(/^ROLE_/, '')
      if (role === 'ADMIN') navigate('/admin/status')
      else if (role === 'CONSULTANT') navigate('/consultant/dashboard')
      else navigate('/client/services')
    } catch (err) {
      const data = err.response?.data
      if (data?.errors?.length) {
        setError(data.errors.map((e) => `${e.field}: ${e.message}`).join(', '))
      } else {
        setError(data?.message ?? data?.error ?? 'Invalid credentials')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="relative min-h-screen flex items-center justify-center bg-[#16171d] overflow-hidden">
      <SmokeyBackground color="#4338ca" />

      <div className="relative z-10 w-full max-w-sm p-8 space-y-6 bg-white/10 backdrop-blur-lg rounded-2xl border border-white/20 shadow-2xl">
        <div className="text-center">
          <h2 className="text-3xl font-bold text-white">Welcome Back</h2>
          <p className="mt-2 text-sm text-gray-300">Sign in to your account</p>
        </div>

        {successMessage && (
          <div className="text-sm text-green-400 bg-green-500/10 border border-green-500/30 rounded-lg px-3 py-2">
            {successMessage}
          </div>
        )}

        {error && (
          <div className="text-sm text-red-400 bg-red-500/10 border border-red-500/30 rounded-lg px-3 py-2">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-8">
          <div className="relative z-0">
            <input
              type="email"
              id="floating_email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder=" "
              className="block py-2.5 px-0 w-full text-sm text-white bg-transparent border-0 border-b-2 border-gray-300 appearance-none focus:outline-none focus:ring-0 focus:border-indigo-500 peer"
            />
            <label
              htmlFor="floating_email"
              className="absolute text-sm text-gray-300 duration-300 transform -translate-y-6 scale-75 top-3 -z-10 origin-[0] peer-focus:text-indigo-400 peer-placeholder-shown:scale-100 peer-placeholder-shown:translate-y-0 peer-focus:scale-75 peer-focus:-translate-y-6"
            >
              <User className="inline-block mr-2 -mt-1" size={16} />
              Email Address
            </label>
          </div>

          <div className="relative z-0">
            <input
              type="password"
              id="floating_password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder=" "
              className="block py-2.5 px-0 w-full text-sm text-white bg-transparent border-0 border-b-2 border-gray-300 appearance-none focus:outline-none focus:ring-0 focus:border-indigo-500 peer"
            />
            <label
              htmlFor="floating_password"
              className="absolute text-sm text-gray-300 duration-300 transform -translate-y-6 scale-75 top-3 -z-10 origin-[0] peer-focus:text-indigo-400 peer-placeholder-shown:scale-100 peer-placeholder-shown:translate-y-0 peer-focus:scale-75 peer-focus:-translate-y-6"
            >
              <Lock className="inline-block mr-2 -mt-1" size={16} />
              Password
            </label>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="group w-full flex items-center justify-center py-3 px-4 bg-indigo-600 hover:bg-indigo-700 rounded-lg text-white font-semibold transition-all duration-300 disabled:opacity-50"
          >
            {loading ? 'Signing in…' : 'Sign in'}
            {!loading && <ArrowRight className="ml-2 h-5 w-5 transform group-hover:translate-x-1 transition-transform" />}
          </button>
        </form>

        <p className="text-center text-xs text-gray-400">
          No account?{' '}
          <Link to="/register" className="font-semibold text-indigo-400 hover:text-indigo-300 transition">
            Create an account
          </Link>
        </p>
      </div>
    </div>
  )
}
