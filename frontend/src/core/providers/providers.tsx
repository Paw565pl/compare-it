"use client";

import { getQueryClient } from "@/core/libs/tanstack-query";
import { QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { NuqsAdapter } from "nuqs/adapters/next/app";
import { ReactNode } from "react";

interface ProvidersProps {
  readonly children: ReactNode;
}

const Providers = ({ children }: ProvidersProps) => {
  const queryClient = getQueryClient();

  return (
    <NuqsAdapter>
      <QueryClientProvider client={queryClient}>
        {children}
        <ReactQueryDevtools initialIsOpen={false} />
      </QueryClientProvider>
    </NuqsAdapter>
  );
};

export default Providers;
