import { useEffect, useRef } from 'react'
import { PromptInputBox } from '../shared/components/PromptInputBox'
import { Bot, User } from 'lucide-react'
import { useAgentChat } from '../shared/chat'
import ChatMessageContent from '../shared/components/ChatMessageContent'

const INITIAL_MESSAGES = [
  {
    id: 1,
    role: 'assistant',
    content:
      "Hi! I'm your ConsultHub assistant. I can help you find the right consultant, explain our services, answer questions about bookings and payments, or anything else you need. How can I help you today?",
  },
]

export default function Chatbot() {
  const { messages, isLoading, sendMessage } = useAgentChat({ initialMessages: INITIAL_MESSAGES })
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  return (
    <div className="flex flex-col h-[calc(100vh-57px)] bg-[#16171d]">
      {/* Header */}
      <div className="border-b border-[#2e303a] px-6 py-4 flex items-center gap-3">
        <div className="w-8 h-8 rounded-full bg-indigo-600 flex items-center justify-center">
          <Bot className="w-4 h-4 text-white" />
        </div>
        <div>
          <p className="text-sm font-semibold text-white">ConsultHub Assistant</p>
          <p className="text-xs text-gray-400">AI-powered support</p>
        </div>
        <div className="ml-auto flex items-center gap-1.5">
          <div className="w-2 h-2 rounded-full bg-green-400 animate-pulse" />
          <span className="text-xs text-gray-400">Online</span>
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-4 py-6 space-y-6">
        <div className="max-w-3xl mx-auto space-y-6">
          {messages.map((msg) => (
            <div key={msg.id} className={`flex gap-3 ${msg.role === 'user' ? 'flex-row-reverse' : 'flex-row'}`}>
              {/* Avatar */}
              <div className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${msg.role === 'user' ? 'bg-indigo-600' : 'bg-[#2e303a]'}`}>
                {msg.role === 'user'
                  ? <User className="w-4 h-4 text-white" />
                  : <Bot className="w-4 h-4 text-gray-300" />
                }
              </div>

              {/* Bubble */}
              <div className={`max-w-[75%] rounded-2xl px-4 py-3 text-sm leading-relaxed ${
                msg.role === 'user'
                  ? 'bg-indigo-600 text-white rounded-tr-sm'
                  : 'bg-[#1F2023] text-gray-100 border border-[#333333] rounded-tl-sm'
              }`}>
                <ChatMessageContent content={msg.content} role={msg.role} />
              </div>
            </div>
          ))}

          {/* Typing indicator */}
          {isLoading && (
            <div className="flex gap-3">
              <div className="w-8 h-8 rounded-full bg-[#2e303a] flex items-center justify-center flex-shrink-0">
                <Bot className="w-4 h-4 text-gray-300" />
              </div>
              <div className="bg-[#1F2023] border border-[#333333] rounded-2xl rounded-tl-sm px-4 py-3 flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-gray-400 animate-bounce" style={{ animationDelay: '0ms' }} />
                <span className="w-2 h-2 rounded-full bg-gray-400 animate-bounce" style={{ animationDelay: '150ms' }} />
                <span className="w-2 h-2 rounded-full bg-gray-400 animate-bounce" style={{ animationDelay: '300ms' }} />
              </div>
            </div>
          )}

          <div ref={bottomRef} />
        </div>
      </div>

      {/* Input */}
      <div className="border-t border-[#2e303a] px-4 py-4">
        <div className="max-w-3xl mx-auto">
          <PromptInputBox onSend={sendMessage} isLoading={isLoading} placeholder="Ask me anything about our consulting services..." />
          <p className="text-xs text-gray-500 text-center mt-2">
            AI assistant may make mistakes. Always verify important information.
          </p>
        </div>
      </div>
    </div>
  )
}
