import './waves.css'

const context = '@@wavesContext'

function isRippleEnabled(value) {
  return value !== false
}

function rippleShow(e) {
  const ripperDiv = e.target.querySelector('.waves-ripple')
  if (ripperDiv) {
    ripperDiv.remove()
  }

  const el = e.target
  const elStyle = el.style
  const elPos = el.getBoundingClientRect()
  const nodeName = el.nodeName.toLowerCase()
  let rippleDiv = document.createElement('div')

  rippleDiv.className = 'waves-ripple'

  const rippleStyle = rippleDiv.style
  const size = Math.max(elPos.width, elPos.height)
  const scale = size * 0.3
  const xPos = e.pageX - elPos.left - window.pageXOffset - scale
  const yPos = e.pageY - elPos.top - window.pageYOffset - scale

  const pos = nodeName === 'input' ? el.parentNode : el
  const oldPos = pos.style.position
  if (!oldPos || oldPos === 'static') {
    pos.style.position = 'relative'
    pos.setAttribute('data-waves-original-position', oldPos)
  }

  rippleStyle.position = 'absolute'
  rippleStyle.top = yPos + 'px'
  rippleStyle.left = xPos + 'px'
  rippleStyle.width = scale * 2 + 'px'
  rippleStyle.height = scale * 2 + 'px'
  rippleStyle.borderRadius = '50%'
  rippleStyle.backgroundColor = 'rgba(0,0,0,0.15)'
  rippleStyle.opacity = '1'
  rippleStyle.transform = 'scale(1)'
  rippleStyle.transition = 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)'

  pos.appendChild(rippleDiv)

  setTimeout(() => {
    rippleStyle.opacity = '0'
    rippleStyle.transform = 'scale(2)'
  }, 0)

  function clearRipple() {
    if (rippleDiv && rippleDiv.parentNode) {
      rippleDiv.parentNode.removeChild(rippleDiv)
    }
    rippleDiv = null

    const originalPosition = pos.getAttribute('data-waves-original-position')
    if (originalPosition) {
      pos.style.position = originalPosition
      pos.removeAttribute('data-waves-original-position')
    } else if (oldPos) {
      pos.style.position = oldPos
    }
  }

  setTimeout(clearRipple, 500)
}

function rippleHide() {
  // Do nothing for now
}

export default {
  bind(el, binding) {
    el.addEventListener('click', rippleShow, false)

    el[context] = {
      removeClick: el => {
        el.removeEventListener('click', rippleShow, false)
      }
    }
  },

  unbind(el) {
    el[context].removeClick(el)
    delete el[context]
  }
}