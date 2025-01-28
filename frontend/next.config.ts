import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  images: {
    domains: ["f00.esfr.pl", "images.morele.net"],
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
