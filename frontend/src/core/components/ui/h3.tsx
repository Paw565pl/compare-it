import { cn } from "@/core/utils/cn";
import { ReactNode } from "react";

interface H3Props {
  children: ReactNode;
  className?: string;
}

export const H3 = ({ children, className }: H3Props) => {
  return (
    <h3 className={cn("text-secondary pt-2 pl-4 text-xl font-bold", className)}>
      {children}
    </h3>
  );
};
