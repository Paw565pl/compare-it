import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  images: {
    domains: ["f00.esfr.pl", "images.morele.net"],
  },
};

export default nextConfig;
