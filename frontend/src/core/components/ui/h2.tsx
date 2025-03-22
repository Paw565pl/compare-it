import { cn } from "@/core/utils/cn";
import { ReactNode } from "react";

interface H2Props {
  children: ReactNode;
  className?: string;
}

export const H2 = ({ children, className }: H2Props) => {
  return (
    <h2
      className={cn(
        "text-primary mb-1 ml-4 text-2xl font-bold sm:ml-0",
        className,
      )}
    >
      {children}
    </h2>
  );
};
