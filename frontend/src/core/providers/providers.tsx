"use client";

import { getClientEnv } from "@/core/libs/env/client-env";
import { getQueryClient } from "@/core/libs/tanstack-query";
import { QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { SessionProvider } from "next-auth/react";
import { NuqsAdapter } from "nuqs/adapters/next/app";
import { ReactNode } from "react";

interface ProvidersProps {
  readonly children: ReactNode;
}

export const Providers = ({ children }: ProvidersProps) => {
  const sessionRefetchInterval = Number(
    getClientEnv("NEXT_PUBLIC_SESSION_REFETCH_INTERVAL"),
  );
  const queryClient = getQueryClient();

  return (
    <SessionProvider refetchInterval={sessionRefetchInterval}>
      <NuqsAdapter>
        <QueryClientProvider client={queryClient}>
          {children}
          <ReactQueryDevtools initialIsOpen={false} />
        </QueryClientProvider>
      </NuqsAdapter>
    </SessionProvider>
  );
};
