import { Link } from 'react-router-dom'
import {
  Briefcase, FileText, Code, TrendingUp, Scale, DollarSign,
  Users, Star, ArrowRight, CheckCircle, Bot, Calendar, CreditCard
} from 'lucide-react'

const SERVICES = [
  { icon: Briefcase,    label: 'Career Coaching',      color: 'text-blue-400',    bg: 'bg-blue-500/10',    desc: 'Accelerate your career with personalised guidance from seasoned professionals.' },
  { icon: FileText,     label: 'Resume Review',         color: 'text-green-400',   bg: 'bg-green-500/10',   desc: 'Stand out with a polished, ATS-ready resume crafted by expert reviewers.' },
  { icon: Code,         label: 'Technical Interview',   color: 'text-indigo-400',  bg: 'bg-indigo-500/10',  desc: 'Ace your next coding interview with mock sessions and real-time feedback.' },
  { icon: Users,        label: 'Mentoring',             color: 'text-orange-400',  bg: 'bg-orange-500/10',  desc: 'One-on-one mentorship to help you grow and reach your goals faster.' },
  { icon: TrendingUp,   label: 'Business Strategy',     color: 'text-yellow-400',  bg: 'bg-yellow-500/10',  desc: 'Turn ideas into action plans with strategic business consulting.' },
  { icon: DollarSign,   label: 'Financial Advisory',    color: 'text-emerald-400', bg: 'bg-emerald-500/10', desc: 'Smart money decisions guided by certified financial experts.' },
  { icon: Scale,        label: 'Legal Consulting',      color: 'text-red-400',     bg: 'bg-red-500/10',     desc: 'Navigate complex legal questions with clarity and confidence.' },
  { icon: Bot,          label: 'AI-Powered Assistant',  color: 'text-purple-400',  bg: 'bg-purple-500/10',  desc: 'Get instant answers and booking help from our 24/7 AI assistant.' },
]

const HOW_IT_WORKS = [
  { icon: Users,    step: '01', title: 'Create an account',      desc: 'Sign up as a client in under a minute. No credit card required to get started.' },
  { icon: Calendar, step: '02', title: 'Browse & book a slot',   desc: 'Explore services, pick a consultant, and choose a time that works for you.' },
  { icon: CreditCard, step: '03', title: 'Pay securely',         desc: 'Complete your payment with our encrypted checkout. Your session is instantly confirmed.' },
  { icon: Star,     step: '04', title: 'Meet your consultant',   desc: 'Attend your session and get expert advice tailored to your unique situation.' },
]

const TESTIMONIALS = [
  { name: 'Sarah K.', role: 'Software Engineer', stars: 5, text: 'The technical interview prep was spot-on. I landed my dream role at a top-tier company within weeks.' },
  { name: 'Marcus T.', role: 'Startup Founder', stars: 5, text: 'The business strategy session helped me completely restructure my go-to-market plan. Game changer.' },
  { name: 'Priya M.', role: 'MBA Graduate', stars: 5, text: 'My resume went from getting zero responses to 3 interviews in the first week after the review.' },
]

function StarRating({ count }) {
  return (
    <div className="flex gap-0.5">
      {Array.from({ length: count }).map((_, i) => (
        <Star key={i} className="w-4 h-4 fill-yellow-400 text-yellow-400" />
      ))}
    </div>
  )
}

export default function Landing() {
  return (
    <div className="min-h-screen bg-[#16171d] text-white">

      {/* ── Navbar ── */}
      <nav className="sticky top-0 z-50 bg-[#16171d]/80 backdrop-blur border-b border-[#2e303a] px-6 py-3 flex items-center justify-between">
        <Link to="/" className="text-xl font-bold text-indigo-400">ConsultHub</Link>
        <div className="hidden md:flex items-center gap-6 text-sm text-gray-400">
          <a href="#services" className="hover:text-white transition-colors">Services</a>
          <a href="#how-it-works" className="hover:text-white transition-colors">How it works</a>
          <a href="#testimonials" className="hover:text-white transition-colors">Reviews</a>
        </div>
        <div className="flex items-center gap-3">
          <Link
            to="/login"
            className="text-sm text-gray-300 hover:text-white px-4 py-1.5 rounded-lg transition-colors"
          >
            Sign in
          </Link>
          <Link
            to="/register"
            className="text-sm bg-indigo-600 hover:bg-indigo-700 text-white font-medium px-4 py-1.5 rounded-lg transition-colors"
          >
            Get started
          </Link>
        </div>
      </nav>

      {/* ── Hero ── */}
      <section className="relative overflow-hidden pt-24 pb-32 px-6 text-center">
        {/* Background glow */}
        <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
          <div className="w-[600px] h-[600px] rounded-full bg-indigo-600/10 blur-3xl" />
        </div>

        <div className="relative max-w-3xl mx-auto">
          <span className="inline-block text-xs font-semibold uppercase tracking-widest text-indigo-400 bg-indigo-500/10 border border-indigo-500/20 rounded-full px-3 py-1 mb-6">
            Expert consulting, on demand
          </span>
          <h1 className="text-5xl sm:text-6xl font-extrabold leading-tight text-white mb-6">
            Get expert advice<br />
            <span className="text-indigo-400">in one click</span>
          </h1>
          <p className="text-lg text-gray-400 max-w-xl mx-auto mb-10">
            Connect with vetted consultants across career, tech, legal, finance, and more.
            Book a session in minutes, pay securely, and get results.
          </p>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link
              to="/register"
              className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold px-7 py-3 rounded-xl transition-colors text-base"
            >
              Start for free <ArrowRight className="w-4 h-4" />
            </Link>
            <a
              href="#services"
              className="flex items-center gap-2 bg-[#1F2023] hover:bg-[#2e303a] border border-[#2e303a] text-gray-200 font-medium px-7 py-3 rounded-xl transition-colors text-base"
            >
              Browse services
            </a>
          </div>

          {/* Stats row */}
          <div className="mt-16 grid grid-cols-3 gap-6 max-w-lg mx-auto">
            {[
              { value: '500+', label: 'Consultants' },
              { value: '12k+', label: 'Sessions booked' },
              { value: '4.9★', label: 'Average rating' },
            ].map(({ value, label }) => (
              <div key={label} className="text-center">
                <p className="text-2xl font-bold text-white">{value}</p>
                <p className="text-xs text-gray-500 mt-1">{label}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── Services ── */}
      <section id="services" className="py-24 px-6 bg-[#1a1b22]">
        <div className="max-w-5xl mx-auto">
          <div className="text-center mb-14">
            <h2 className="text-3xl font-bold text-white">What we offer</h2>
            <p className="text-gray-400 mt-3 max-w-xl mx-auto">
              From career pivots to legal questions, our consultants cover every domain you need.
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
            {SERVICES.map(({ icon: Icon, label, color, bg, desc }) => (
              <div
                key={label}
                className="bg-[#1F2023] border border-[#2e303a] rounded-2xl p-5 hover:border-indigo-500/40 transition-colors group"
              >
                <div className={`w-10 h-10 ${bg} rounded-xl flex items-center justify-center mb-4`}>
                  <Icon className={`w-5 h-5 ${color}`} />
                </div>
                <h3 className="text-sm font-semibold text-gray-100 mb-2">{label}</h3>
                <p className="text-xs text-gray-500 leading-relaxed">{desc}</p>
              </div>
            ))}
          </div>

          <div className="text-center mt-10">
            <Link
              to="/services"
              className="inline-flex items-center gap-2 text-sm text-indigo-400 hover:text-indigo-300 font-medium transition-colors"
            >
              See all available services <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </div>
      </section>

      {/* ── How it works ── */}
      <section id="how-it-works" className="py-24 px-6">
        <div className="max-w-4xl mx-auto">
          <div className="text-center mb-14">
            <h2 className="text-3xl font-bold text-white">How it works</h2>
            <p className="text-gray-400 mt-3">Book your first session in under 5 minutes.</p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {HOW_IT_WORKS.map(({ icon: Icon, step, title, desc }) => (
              <div key={step} className="relative">
                <div className="bg-[#1F2023] border border-[#2e303a] rounded-2xl p-6 h-full">
                  <span className="text-xs font-bold text-indigo-500/60 tracking-widest">{step}</span>
                  <div className="w-10 h-10 bg-indigo-600/15 rounded-xl flex items-center justify-center my-3">
                    <Icon className="w-5 h-5 text-indigo-400" />
                  </div>
                  <h3 className="text-sm font-semibold text-gray-100 mb-2">{title}</h3>
                  <p className="text-xs text-gray-500 leading-relaxed">{desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── Why ConsultHub ── */}
      <section className="py-20 px-6 bg-[#1a1b22]">
        <div className="max-w-4xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
          <div>
            <h2 className="text-3xl font-bold text-white mb-4">
              Why choose<br /><span className="text-indigo-400">ConsultHub?</span>
            </h2>
            <p className="text-gray-400 text-sm leading-relaxed mb-8">
              We vet every consultant so you don't have to. Our platform makes it simple to
              find the right expert, schedule at your convenience, and pay with confidence.
            </p>
            <ul className="space-y-3">
              {[
                'Verified, screened professionals',
                'Flexible scheduling — book any time',
                'Secure, encrypted payments',
                'AI assistant available 24/7',
                'Money-back guarantee on your first session',
              ].map((item) => (
                <li key={item} className="flex items-center gap-3 text-sm text-gray-300">
                  <CheckCircle className="w-4 h-4 text-green-400 flex-shrink-0" />
                  {item}
                </li>
              ))}
            </ul>
          </div>

          {/* Visual card */}
          <div className="bg-[#1F2023] border border-[#2e303a] rounded-2xl p-6 space-y-4">
            <p className="text-xs uppercase tracking-widest text-gray-500 font-semibold">Live session preview</p>
            {[
              { name: 'Alex Chen',    role: 'Tech Interview Coach', time: 'Today, 2:00 PM',   avail: true  },
              { name: 'Jordan Lee',   role: 'Resume Specialist',    time: 'Today, 4:30 PM',   avail: true  },
              { name: 'Maria Santos', role: 'Financial Advisor',    time: 'Tomorrow, 10 AM',  avail: false },
            ].map(({ name, role, time, avail }) => (
              <div key={name} className="flex items-center gap-3 bg-[#16171d] rounded-xl p-3">
                <div className="w-9 h-9 rounded-full bg-indigo-600/30 text-indigo-400 font-bold flex items-center justify-center text-sm flex-shrink-0">
                  {name[0]}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-100 truncate">{name}</p>
                  <p className="text-xs text-gray-500 truncate">{role}</p>
                </div>
                <div className="text-right flex-shrink-0">
                  <p className="text-xs text-gray-400">{time}</p>
                  <span className={`text-xs font-medium ${avail ? 'text-green-400' : 'text-gray-500'}`}>
                    {avail ? 'Available' : 'Booked'}
                  </span>
                </div>
              </div>
            ))}
            <Link
              to="/register"
              className="block w-full text-center text-sm font-medium bg-indigo-600 hover:bg-indigo-700 text-white py-2.5 rounded-xl transition-colors mt-2"
            >
              Book a session →
            </Link>
          </div>
        </div>
      </section>

      {/* ── Testimonials ── */}
      <section id="testimonials" className="py-24 px-6">
        <div className="max-w-4xl mx-auto">
          <div className="text-center mb-14">
            <h2 className="text-3xl font-bold text-white">What our clients say</h2>
            <p className="text-gray-400 mt-3">Real results from real people.</p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
            {TESTIMONIALS.map(({ name, role, stars, text }) => (
              <div
                key={name}
                className="bg-[#1F2023] border border-[#2e303a] rounded-2xl p-6 flex flex-col gap-4"
              >
                <StarRating count={stars} />
                <p className="text-sm text-gray-300 leading-relaxed flex-1">"{text}"</p>
                <div>
                  <p className="text-sm font-semibold text-gray-100">{name}</p>
                  <p className="text-xs text-gray-500">{role}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── CTA ── */}
      <section className="py-24 px-6 bg-[#1a1b22]">
        <div className="max-w-2xl mx-auto text-center">
          <div className="w-16 h-16 bg-indigo-600/20 rounded-2xl flex items-center justify-center mx-auto mb-6">
            <Bot className="w-8 h-8 text-indigo-400" />
          </div>
          <h2 className="text-3xl font-bold text-white mb-4">Ready to get started?</h2>
          <p className="text-gray-400 mb-8">
            Create your free account today and book your first session with an expert consultant.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/register"
              className="flex items-center justify-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold px-8 py-3 rounded-xl transition-colors"
            >
              Create free account <ArrowRight className="w-4 h-4" />
            </Link>
            <Link
              to="/login"
              className="flex items-center justify-center gap-2 bg-[#1F2023] hover:bg-[#2e303a] border border-[#2e303a] text-gray-200 font-medium px-8 py-3 rounded-xl transition-colors"
            >
              Sign in
            </Link>
          </div>
        </div>
      </section>

      {/* ── Footer ── */}
      <footer className="border-t border-[#2e303a] px-6 py-8">
        <div className="max-w-5xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
          <Link to="/" className="text-lg font-bold text-indigo-400">ConsultHub</Link>
          <p className="text-xs text-gray-600">© 2026 ConsultHub. All rights reserved.</p>
          <div className="flex items-center gap-6 text-xs text-gray-500">
            <a href="#services" className="hover:text-gray-300 transition-colors">Services</a>
            <Link to="/register" className="hover:text-gray-300 transition-colors">Sign up</Link>
            <Link to="/login" className="hover:text-gray-300 transition-colors">Sign in</Link>
          </div>
        </div>
      </footer>
    </div>
  )
}
