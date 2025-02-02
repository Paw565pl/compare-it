import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  images: {
    remotePatterns: [
      {
        protocol: "https",
        hostname: "f00.esfr.pl",
      },
      {
        protocol: "https",
        hostname: "images.morele.net",
      },
      {
        protocol: "https",
        hostname: "galeriachelm.com",
      },
      {
        protocol: "https",
        hostname: "www.morele.net",
      },
    ],
  },
  async redirects() {
    return [
      {
        source: "/",
        destination: "/produkty",
        permanent: true,
      },
    ];
  },
};

export default nextConfig;
