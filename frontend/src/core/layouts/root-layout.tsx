import "@/app/globals.css";
import { Navbar } from "@/core/components/navbar";
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
          <main className="container mx-auto p-2">{children}</main>
        </Providers>
      </body>
    </html>
  );
};

export default RootLayout;
