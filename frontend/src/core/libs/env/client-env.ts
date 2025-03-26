import { createEnv } from "@t3-oss/env-nextjs";
import { z } from "zod";

const clientEnv = createEnv({
  client: {
    NEXT_PUBLIC_API_BASE_URL: z.string().url(),
    NEXT_PUBLIC_AUTH_AUTH0_ISSUER: z.string().url(),
    NEXT_PUBLIC_AUTH_AUTH0_ID: z.string(),
    NEXT_PUBLIC_ACCESS_TOKEN_LIFETIME: z.number().positive(),
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

export default clientEnv;
