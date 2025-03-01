import "@/app/globals.css";
import { Navbar } from "@/core/components";
import { Toaster } from "@/core/components/ui/sonner";
import Providers from "@/core/providers/providers";
import type { Metadata } from "next";
import { ReactNode } from "react";

export const metadata: Metadata = {
  title: "compare.it",
};

interface RootLayoutProps {
  readonly children: ReactNode;
}

const RootLayout = ({ children }: RootLayoutProps) => {
  return (
    <html lang="en">
      <body className="antialiased">
        <Providers>
          <Navbar />
          <main className="bg-background container mx-auto px-2 pb-4">
            {children}
          </main>
          <Toaster closeButton />
        </Providers>
      </body>
    </html>
  );
};

export default RootLayout;
