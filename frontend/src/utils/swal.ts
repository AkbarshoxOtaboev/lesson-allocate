import Swal from 'sweetalert2'

const base = Swal.mixin({
  confirmButtonColor: '#465fff',
  cancelButtonColor: '#98a2b3',
  buttonsStyling: true,
})

export function showError(message: string, title = 'Xatolik') {
  return base.fire({
    icon: 'error',
    title,
    text: message,
    confirmButtonText: 'OK',
  })
}

export function showSuccess(message: string, title = 'Muvaffaqiyatli') {
  return base.fire({
    icon: 'success',
    title,
    text: message,
    confirmButtonText: 'OK',
    timer: 2200,
    timerProgressBar: true,
  })
}

export function showWarning(message: string, title = 'Diqqat') {
  return base.fire({
    icon: 'warning',
    title,
    text: message,
    confirmButtonText: 'OK',
  })
}

export async function confirmAction(
  message: string,
  title = 'Tasdiqlash',
): Promise<boolean> {
  const result = await base.fire({
    icon: 'question',
    title,
    text: message,
    showCancelButton: true,
    confirmButtonText: 'Ha',
    cancelButtonText: "Yo'q",
    reverseButtons: true,
  })
  return result.isConfirmed
}

export { Swal }
