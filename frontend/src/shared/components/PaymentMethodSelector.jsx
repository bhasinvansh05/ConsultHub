import * as React from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Trash2 } from 'lucide-react'

const PlusIcon = ({ className }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="24" height="24"
    viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
    strokeLinecap="round" strokeLinejoin="round">
    <line x1="12" y1="5" x2="12" y2="19" />
    <line x1="5" y1="12" x2="19" y2="12" />
  </svg>
)

export function PaymentMethodSelector({ title, actionText, methods, defaultSelectedId, onActionClick, onSelectionChange, onDelete }) {
  const [selectedId, setSelectedId] = React.useState(
    defaultSelectedId ?? (methods.length > 0 ? methods[0].id : null)
  )

  const handleSelect = (id) => {
    setSelectedId(id)
    onSelectionChange?.(id)
  }

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: { opacity: 1, transition: { staggerChildren: 0.08 } },
  }

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { opacity: 1, y: 0 },
  }

  return (
    <div className="w-full rounded-xl border border-[#2e303a] bg-[#1F2023] p-6">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-xl font-semibold text-white">{title}</h3>
        {actionText && (
          <button
            onClick={onActionClick}
            className="flex items-center gap-1 text-sm font-medium text-indigo-400 hover:text-indigo-300 transition-colors"
          >
            <PlusIcon className="w-4 h-4" />
            {actionText}
          </button>
        )}
      </div>

      <motion.div
        className="space-y-3"
        variants={containerVariants}
        initial="hidden"
        animate="visible"
        role="radiogroup"
      >
        {methods.map((method) => {
          const isSelected = selectedId === method.id
          return (
            <motion.div
              key={method.id}
              variants={itemVariants}
              onClick={() => handleSelect(method.id)}
              onKeyDown={(e) => (e.key === ' ' || e.key === 'Enter') && handleSelect(method.id)}
              className="group flex items-center p-4 rounded-lg border cursor-pointer transition-all duration-200 hover:bg-[#16171d]"
              style={{
                borderColor: isSelected ? '#6366f1' : '#2e303a',
                boxShadow: isSelected ? '0 0 0 1px #6366f1' : 'none',
              }}
              role="radio"
              aria-checked={isSelected}
              tabIndex={0}
            >
              <div className="flex-shrink-0 text-gray-300">{method.icon}</div>
              <div className="ml-4 flex-grow">
                <p className="font-medium text-gray-100">{method.label}</p>
                <p className="text-sm text-gray-500">{method.description}</p>
              </div>

              {onDelete && (
                <button
                  onClick={(e) => { e.stopPropagation(); onDelete(method.id) }}
                  className="ml-2 opacity-0 group-hover:opacity-100 transition-opacity p-1 rounded-md text-red-400 hover:text-red-300 hover:bg-red-500/10 flex-shrink-0"
                  title="Remove"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              )}

              <div
                className="ml-3 flex h-5 w-5 items-center justify-center rounded-full border-2 flex-shrink-0"
                style={{ borderColor: isSelected ? '#6366f1' : '#444' }}
              >
                <AnimatePresence>
                  {isSelected && (
                    <motion.div
                      initial={{ scale: 0 }}
                      animate={{ scale: 1 }}
                      exit={{ scale: 0 }}
                      className="h-2.5 w-2.5 rounded-full bg-indigo-500"
                    />
                  )}
                </AnimatePresence>
              </div>
            </motion.div>
          )
        })}
      </motion.div>
    </div>
  )
}
