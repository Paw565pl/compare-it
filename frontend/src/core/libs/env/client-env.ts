import { createEnv } from "@t3-oss/env-nextjs";
import { env } from "next-runtime-env";
import { z } from "zod";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const clientEnv = createEnv({
  client: {
    NEXT_PUBLIC_API_BASE_URL: z.string().trim().url(),
    NEXT_PUBLIC_AUTH_AUTH0_ISSUER: z.string().trim().url(),
    NEXT_PUBLIC_AUTH_AUTH0_ID: z.string().trim(),
    NEXT_PUBLIC_ACCESS_TOKEN_LIFETIME: z.coerce.number().positive(),
  },
  runtimeEnv: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL,
    NEXT_PUBLIC_AUTH_AUTH0_ISSUER: process.env.NEXT_PUBLIC_AUTH_AUTH0_ISSUER,
    NEXT_PUBLIC_AUTH_AUTH0_ID: process.env.NEXT_PUBLIC_AUTH_AUTH0_ID,
    NEXT_PUBLIC_ACCESS_TOKEN_LIFETIME:
      process.env.NEXT_PUBLIC_ACCESS_TOKEN_LIFETIME,
  },
  skipValidation: process.env.CI ? true : false,
});

type ClientEnvKey = keyof typeof clientEnv;

export const getClientEnv = (key: ClientEnvKey) => env(key);
