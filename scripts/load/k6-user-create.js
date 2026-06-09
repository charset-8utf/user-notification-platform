import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: 5,
  duration: "30s",
  thresholds: {
    http_req_failed: ["rate<0.1"],
    http_req_duration: ["p(95)<5000"],
  },
};

const baseUrl = __ENV.K6_USER_HTTPS || "https://localhost:8443";
const adminUser = __ENV.K6_ADMIN_USER || "admin";
const adminPass = __ENV.K6_ADMIN_PASS || "admin123";

export default function () {
  const loginRes = http.post(
    `${baseUrl}/api/auth/login`,
    JSON.stringify({ username: adminUser, password: adminPass }),
    {
      headers: { "Content-Type": "application/json" },
      insecureSkipTLSVerify: true,
    }
  );

  check(loginRes, { "login status 200": (r) => r.status === 200 });

  const token = loginRes.json("accessToken");
  if (!token) {
    return;
  }

  const email = `k6-${__VU}-${Date.now()}@example.com`;
  const createRes = http.post(
    `${baseUrl}/api/users`,
    JSON.stringify({ name: "K6 User", email, age: 25 }),
    {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      insecureSkipTLSVerify: true,
    }
  );

  check(createRes, { "create user 201": (r) => r.status === 201 });
  sleep(1);
}
