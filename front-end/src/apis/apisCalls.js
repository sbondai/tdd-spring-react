import axios from "axios";

export const signup = (user) => {
  return axios.post("/api/v1/users", user);
};

export const login = (user) => {
  return axios.post("/api/v1/login", {}, { auth: user });
};

export const setAuthorizationHeader = ({ username, password, isLoggedIn }) => {
  if (isLoggedIn) {
    axios.defaults.headers.common["Authorization"] = `Basic ${btoa(
      username + ":" + password
    )}`;
  } else {
    delete axios.defaults.headers.common["Authorization"];
  }
};

export const listUsers = (param = { page: 0, size: 3 }) => {
  const path = `/api/v1/users?page=${param.page || 0}&size=${param.size || 3}`;
  return axios.get(path);
};

export const getUser = (username) => {
  return axios.get(`/api/v1/users/${username}`);
};

export const updateUser = (userId, body) => {
  return axios.put("/api/v1/users/" + userId, body);
};

export const postHoax = (hoax) => {
  return axios.post("/api/v1/hoaxes", hoax);
};

export const loadHoaxes = (username) => {
  const basePath = username
    ? `/api/v1/users/${username}/hoaxes`
    : "/api/v1/hoaxes";
  return axios.get(basePath + "?page=0&size=5&sort=id,desc");
};

export const loadOldHoaxes = (hoaxId, username) => {
  const basePath = username
    ? `/api/v1/users/${username}/hoaxes`
    : "/api/v1/hoaxes";
  const path = `${basePath}/${hoaxId}?direction=before&page=0&size=5&sort=id,desc`;
  return axios.get(path);
};

export const loadNewHoaxes = (hoaxId, username) => {
  const basePath = username
    ? `/api/v1/users/${username}/hoaxes`
    : "/api/v1/hoaxes";
  const path = `${basePath}/${hoaxId}?direction=after&sort=id,desc`;
  return axios.get(path);
};

export const loadNewHoaxCount = (hoaxId, username) => {
  const basePath = username
    ? `/api/v1/users/${username}/hoaxes`
    : "/api/v1/hoaxes";
  const path = `${basePath}/${hoaxId}?direction=after&count=true`;
  return axios.get(path);
};

export const postHoaxFile = (file) => {
  return axios.post("/api/v1/hoaxes/upload", file);
};

export const deleteHoax = (hoaxId) => {
  return axios.delete("/api/v1/hoaxes/" + hoaxId);
};
