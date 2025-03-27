"use client";

import clientEnv from "@/core/libs/env/client-env";
import { getQueryClient } from "@/core/libs/tanstack-query";
import { QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { SessionProvider } from "next-auth/react";
import { NuqsAdapter } from "nuqs/adapters/next/app";
import { ReactNode } from "react";

interface ProvidersProps {
  readonly children: ReactNode;
}

const Providers = ({ children }: ProvidersProps) => {
  const accessTokenLifetime = clientEnv.NEXT_PUBLIC_ACCESS_TOKEN_LIFETIME;
  const queryClient = getQueryClient();

  return (
    <SessionProvider refetchInterval={accessTokenLifetime}>
      <NuqsAdapter>
        <QueryClientProvider client={queryClient}>
          {children}
          <ReactQueryDevtools initialIsOpen={false} />
        </QueryClientProvider>
      </NuqsAdapter>
    </SessionProvider>
  );
};

export default Providers;
