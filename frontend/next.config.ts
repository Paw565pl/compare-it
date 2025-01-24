import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  output: "standalone",
};

module.exports = {
  images: {
    domains: ["f00.esfr.pl", "images.morele.net"],
  },
};

export default nextConfig;
