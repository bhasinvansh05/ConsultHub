import { useState, useRef, useEffect } from 'react'
import { Bot, User, X, MessageCircle, Maximize2, Minimize2 } from 'lucide-react'
import { PromptInputBox } from './PromptInputBox'
import { useAgentChat } from '../chat'
import ChatMessageContent from './ChatMessageContent'

const INITIAL_MESSAGES = [
  {
    id: 1,
    role: 'assistant',
    content: "Hi! I'm your ConsultHub assistant. How can I help you today?",
  },
]

export default function ChatWidget() {
  const [open, setOpen] = useState(false)
  const [isFullscreen, setIsFullscreen] = useState(false)
  const { messages, isLoading, sendMessage } = useAgentChat({
    initialMessages: INITIAL_MESSAGES,
  })
  const bottomRef = useRef(null)

  const closePanel = () => {
    setOpen(false)
    setIsFullscreen(false)
  }

  useEffect(() => {
    if (open) bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, open])

  const panelWrapperClass = isFullscreen
    ? 'fixed inset-0 z-50 bg-[#16171d]/95 backdrop-blur-sm p-4 sm:p-6'
    : ''

  const panelClass = isFullscreen
    ? 'h-full w-full max-w-5xl mx-auto rounded-2xl border border-[#2e303a] bg-[#16171d] shadow-2xl flex flex-col overflow-hidden'
    : 'w-[360px] rounded-2xl border border-[#2e303a] bg-[#16171d] shadow-2xl flex flex-col overflow-hidden'

  const messageContainerClass = isFullscreen
    ? 'flex-1 overflow-y-auto px-4 py-6 space-y-5'
    : 'flex-1 overflow-y-auto px-3 py-4 space-y-4'

  const bubbleClass = (role) => `rounded-2xl text-sm leading-relaxed ${
    role === 'user'
      ? 'bg-indigo-600 text-white rounded-tr-sm'
      : 'bg-[#1F2023] text-gray-100 border border-[#333333] rounded-tl-sm'
  } ${isFullscreen ? 'max-w-[75%] px-4 py-3' : 'max-w-[80%] px-3 py-2'}`

  return (
    <div className={`fixed z-50 flex flex-col gap-3 ${isFullscreen ? 'inset-0' : 'bottom-6 right-6 items-end'}`}>
      {/* Chat Panel */}
      {open && (
        <div className={panelWrapperClass}>
          <div className={panelClass} style={isFullscreen ? undefined : { height: '500px' }}>
            {/* Header */}
            <div className={`border-b border-[#2e303a] flex items-center gap-2 flex-shrink-0 ${isFullscreen ? 'px-6 py-4' : 'px-4 py-3'}`}>
              <div className={`rounded-full bg-indigo-600 flex items-center justify-center ${isFullscreen ? 'w-8 h-8' : 'w-7 h-7'}`}>
                <Bot className={`${isFullscreen ? 'w-4 h-4' : 'w-3.5 h-3.5'} text-white`} />
              </div>
              <div className="flex-1 min-w-0">
                <p className={`font-semibold text-white leading-none ${isFullscreen ? 'text-base' : 'text-sm'}`}>ConsultHub Assistant</p>
                <p className="text-xs text-gray-400 mt-0.5">AI-powered support</p>
              </div>
              <div className="flex items-center gap-1.5 mr-1">
                <div className={`${isFullscreen ? 'w-2 h-2' : 'w-1.5 h-1.5'} rounded-full bg-green-400 animate-pulse`} />
                <span className="text-xs text-gray-400">Online</span>
              </div>
              <button
                type="button"
                onClick={() => setIsFullscreen((value) => !value)}
                className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-[#2e303a] text-gray-400 hover:text-white transition-colors"
                aria-label={isFullscreen ? 'Exit full screen chat' : 'Open full screen chat'}
              >
                {isFullscreen ? <Minimize2 className="w-4 h-4" /> : <Maximize2 className="w-4 h-4" />}
              </button>
              <button
                type="button"
                onClick={closePanel}
                className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-[#2e303a] text-gray-400 hover:text-white transition-colors"
                aria-label="Close chat"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Messages */}
            <div className={messageContainerClass}>
              {messages.map((msg) => (
                <div key={msg.id} className={`flex gap-2 ${msg.role === 'user' ? 'flex-row-reverse' : 'flex-row'}`}>
                  <div className={`rounded-full flex items-center justify-center flex-shrink-0 ${msg.role === 'user' ? 'bg-indigo-600' : 'bg-[#2e303a]'} ${isFullscreen ? 'w-8 h-8' : 'w-6 h-6'}`}>
                    {msg.role === 'user'
                      ? <User className={`${isFullscreen ? 'w-4 h-4' : 'w-3 h-3'} text-white`} />
                      : <Bot className={`${isFullscreen ? 'w-4 h-4' : 'w-3 h-3'} text-gray-300`} />
                    }
                  </div>
                  <div className={bubbleClass(msg.role)}>
                    <ChatMessageContent content={msg.content} role={msg.role} />
                  </div>
                </div>
              ))}

              {isLoading && (
                <div className="flex gap-2">
                  <div className={`rounded-full bg-[#2e303a] flex items-center justify-center flex-shrink-0 ${isFullscreen ? 'w-8 h-8' : 'w-6 h-6'}`}>
                    <Bot className={`${isFullscreen ? 'w-4 h-4' : 'w-3 h-3'} text-gray-300`} />
                  </div>
                  <div className={`bg-[#1F2023] border border-[#333333] rounded-2xl rounded-tl-sm flex items-center gap-1 ${isFullscreen ? 'px-4 py-3' : 'px-3 py-2'}`}>
                    <span className={`${isFullscreen ? 'w-2 h-2' : 'w-1.5 h-1.5'} rounded-full bg-gray-400 animate-bounce`} style={{ animationDelay: '0ms' }} />
                    <span className={`${isFullscreen ? 'w-2 h-2' : 'w-1.5 h-1.5'} rounded-full bg-gray-400 animate-bounce`} style={{ animationDelay: '150ms' }} />
                    <span className={`${isFullscreen ? 'w-2 h-2' : 'w-1.5 h-1.5'} rounded-full bg-gray-400 animate-bounce`} style={{ animationDelay: '300ms' }} />
                  </div>
                </div>
              )}
              <div ref={bottomRef} />
            </div>

            {/* Input */}
            <div className={`border-t border-[#2e303a] flex-shrink-0 ${isFullscreen ? 'px-6 py-4' : 'p-3'}`}>
              <PromptInputBox
                onSend={sendMessage}
                isLoading={isLoading}
                placeholder={isFullscreen ? 'Ask me anything about our consulting services...' : 'Ask me anything...'}
              />
              {isFullscreen && (
                <p className="text-xs text-gray-500 text-center mt-2">
                  AI assistant may make mistakes. Always verify important information.
                </p>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Toggle Button */}
      <button
        type="button"
        onClick={() => (open ? closePanel() : setOpen(true))}
        className="w-12 h-12 rounded-full bg-indigo-600 hover:bg-indigo-700 text-white flex items-center justify-center shadow-lg transition-colors"
      >
        {open ? <X className="w-5 h-5" /> : <MessageCircle className="w-5 h-5" />}
      </button>
    </div>
  )
}
