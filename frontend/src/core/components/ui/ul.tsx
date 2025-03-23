import { cn } from "@/core/utils/cn";
import { ReactNode } from "react";

interface UlProps {
  children: ReactNode;
  className?: string;
}

export const Ul = ({ children, className }: UlProps) => {
  return <ul className={cn("mt-2 w-full bg-white", className)}>{children}</ul>;
};
