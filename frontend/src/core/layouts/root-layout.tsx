import "@/app/globals.css";
import { Navbar } from "@/core/components";
import { Toaster } from "@/core/components/ui/sonner";
import { Providers } from "@/core/providers/providers";
import { PublicEnvScript } from "next-runtime-env";
import { Noto_Sans_Display } from "next/font/google";
import { ReactNode } from "react";

const notoSansDisplay = Noto_Sans_Display({
  subsets: ["latin-ext"],
  display: "swap",
  variable: "--font-noto-sans-display",
});

interface RootLayoutProps {
  readonly children: ReactNode;
}

const RootLayout = ({ children }: RootLayoutProps) => {
  return (
    <html lang="pl" className={notoSansDisplay.variable}>
      {/* eslint-disable-next-line @next/next/no-head-element */}
      <head>
        <PublicEnvScript />
      </head>
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
