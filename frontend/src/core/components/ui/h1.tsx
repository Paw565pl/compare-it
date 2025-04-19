import { cn } from "@/core/utils/cn";
import { ReactNode } from "react";

interface H1Props {
  children: ReactNode;
  className?: string;
}

export const H1 = ({ children, className }: H1Props) => {
  return (
    <h1 className={cn("mb-5 text-4xl font-bold sm:mb-4", className)}>
      {children}
    </h1>
  );
};
