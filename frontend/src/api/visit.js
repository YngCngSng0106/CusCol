import http from '../utils/request'

export function pageVisits(params) {
  return http.get('/visit/page', { params })
}

export function listCustomerVisits(customerId) {
  return http.get(`/visit/customer/${customerId}`)
}

export function getVisit(id) {
  return http.get(`/visit/${id}`)
}

export function createVisit(data) {
  return http.post('/visit', data)
}

export function updateVisit(id, data) {
  return http.put(`/visit/${id}`, data)
}

export function deleteVisit(id) {
  return http.delete(`/visit/${id}`)
}
