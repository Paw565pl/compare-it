import { createEnv } from "@t3-oss/env-nextjs";
import { env } from "next-runtime-env";
import { z } from "zod";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const clientEnv = createEnv({
  client: {
    NEXT_PUBLIC_API_BASE_URL: z.url().trim(),
    NEXT_PUBLIC_AUTH_AUTH0_ISSUER: z.url().trim(),
    NEXT_PUBLIC_AUTH_AUTH0_ID: z.string().trim(),
    NEXT_PUBLIC_SESSION_REFETCH_INTERVAL: z.coerce.number().positive(),
  },
  runtimeEnv: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL,
    NEXT_PUBLIC_AUTH_AUTH0_ISSUER: process.env.NEXT_PUBLIC_AUTH_AUTH0_ISSUER,
    NEXT_PUBLIC_AUTH_AUTH0_ID: process.env.NEXT_PUBLIC_AUTH_AUTH0_ID,
    NEXT_PUBLIC_SESSION_REFETCH_INTERVAL:
      process.env.NEXT_PUBLIC_SESSION_REFETCH_INTERVAL,
  },
  skipValidation: true,
});

type ClientEnvKey = keyof typeof clientEnv;

export const getClientEnv = (key: ClientEnvKey) => env(key);
