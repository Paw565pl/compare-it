import { cn } from "@/core/utils/cn";
import { ReactNode } from "react";

interface H1Props {
  children: ReactNode;
  className?: string;
}

export const Ul = ({ children, className }: H1Props) => {
  return <ul className={cn("mt-2 w-full bg-white", className)}>{children}</ul>;
};
