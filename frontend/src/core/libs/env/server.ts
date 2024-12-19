import { createEnv } from "@t3-oss/env-nextjs";
import { z } from "zod";

const serverEnv = createEnv({
  server: {
    API_BASE_URL: z.string().url(),
  },
  experimental__runtimeEnv: process.env,
});

export default serverEnv;
