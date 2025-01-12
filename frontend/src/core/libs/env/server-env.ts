import { createEnv } from "@t3-oss/env-nextjs";
import { z } from "zod";

const serverEnv = createEnv({
  server: {
    API_BASE_URL: z.string().url(),
    AUTH_SECRET: z
      .string()
      .min(32, "Auth secret must be at least 32 chars long.")
      .max(128, "Auth secret must be at most 128 chars long."),
    AUTH_TRUST_HOST: z.enum(["true", "false"]),
    AUTH_AUTH0_ISSUER: z.string().url(),
    AUTH_AUTH0_ID: z.string(),
    AUTH_AUTH0_SECRET: z.string(),
  },
  experimental__runtimeEnv: process.env,
  skipValidation: process.env.CI ? true : false,
});

export default serverEnv;
