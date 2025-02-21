import http from 'k6/http'
import { sleep } from 'k6'

export let options = {
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1500'],
  },
  stages : [
    { duration: "5s", target: 50 },
    { duration: "10s", target: 500 },
    { duration: "10s", target: 500 },
    { duration: "5s", target: 0 },
  ]
}

export default function () {
  const BASE_URL = 'http://localhost:7056'

  // http.get(`${BASE_URL}`)
  // sleep(0.05)

  http.get(`${BASE_URL}/kafka`)
  sleep(0.05)

}

