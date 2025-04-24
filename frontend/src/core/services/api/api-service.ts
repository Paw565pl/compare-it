import clientEnv from "@/core/libs/env/client-env";
import serverEnv from "@/core/libs/env/server-env";
import axios from "axios";

const baseUrl =
  typeof window === "undefined"
    ? serverEnv.API_BASE_URL
    : clientEnv.NEXT_PUBLIC_API_BASE_URL;

export const apiService = axios.create({ baseURL: baseUrl, adapter: "fetch" });
