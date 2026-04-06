import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'

export default function ChatMessageContent({ content, role }) {
  if (role === 'user') {
    return <span>{content}</span>
  }

  return (
    <div className="chat-markdown">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          a: ({ node, ...props }) => (
            <a
              {...props}
              target="_blank"
              rel="noreferrer noopener"
              className="text-indigo-300 underline hover:text-indigo-200"
            />
          ),
          code: ({ inline, className, children, ...props }) => {
            if (inline) {
              return (
                <code
                  {...props}
                  className="bg-[#2a2b30] text-gray-100 px-1 py-0.5 rounded text-[0.85em] font-mono"
                >
                  {children}
                </code>
              )
            }
            return (
              <pre className="bg-[#111216] border border-[#333333] rounded-md p-3 overflow-x-auto my-2">
                <code {...props} className={className}>
                  {children}
                </code>
              </pre>
            )
          },
          p: ({ node, ...props }) => <p {...props} className="mb-2 last:mb-0" />,
          ul: ({ node, ...props }) => <ul {...props} className="list-disc pl-5 my-2" />,
          ol: ({ node, ...props }) => <ol {...props} className="list-decimal pl-5 my-2" />,
          li: ({ node, ...props }) => <li {...props} className="mb-1" />,
          blockquote: ({ node, ...props }) => (
            <blockquote
              {...props}
              className="border-l-2 border-gray-500 pl-3 text-gray-300 my-2 italic"
            />
          ),
        }}
      >
        {content || ''}
      </ReactMarkdown>
    </div>
  )
}
