import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  transpilePackages: ["@t3-oss/env-nextjs", "@t3-oss/env-core"],
  typedRoutes: true,
  experimental: {
    reactCompiler: true,
  },
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
        hostname: "prod-api.mediaexpert.pl",
      },
    ],
  },
  redirects: async () => {
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
