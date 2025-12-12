import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const email = ref('')

  const setEmail = (newEmail: string) => {
    email.value = newEmail
  }

  return { email, setEmail }
})
