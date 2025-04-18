import { Slot } from "@radix-ui/react-slot";
import { cva, type VariantProps } from "class-variance-authority";

import { cn } from "@/core/utils/cn";
import { ButtonHTMLAttributes, forwardRef } from "react";

const buttonVariants = cva(
  "inline-flex items-center justify-center gap-2 whitespace-nowrap text-sm font-medium cursor-pointer transition-colors focus-visible:outline-hidden focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0",
  {
    variants: {
      variant: {
        default:
          "bg-primary text-primary-foreground shadow-sm hover:bg-primary/90",
        destructive:
          "bg-destructive text-destructive-foreground shadow-xs hover:bg-destructive/85",
        outline:
          "border border-input bg-white shadow-xs hover:bg-accent hover:text-accent-foreground",
        primary: "bg-primary text-primary-foreground shadow-xs hover:bg-hover",
        secondary:
          "bg-secondary text-secondary-foreground shadow-xs hover:bg-hover",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
        category: "flex w-full justify-start px-4 py-2",
        filter: "bg-primary hover:bg-hover px-4 py-2 text-white w-full",
        sort: "bg-primary hover:bg-hover px-4 py-2 text-white",
        sortCategory:
          "flex w-full justify-start px-4 py-2 hover:bg-hover hover:text-white",
        pagination:
          "bg-primary hover:bg-hover m-4 mt-0 px-4 py-2 text-white disabled:bg-gray-500 sm:m-0",
        priceAlert: "text-primary cursor-pointer shadow-none",
        search:
          "bg-primary hover:bg-hover flex items-center p-2 font-medium text-white transition-colors duration-300",
      },
      size: {
        default: "h-9 px-4 py-2",
        sm: "h-8 px-3 text-xs",
        lg: "h-10 px-8",
        icon: "h-9 w-9",
        noPadding: "p-0 h-auto w-auto",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  },
);

export interface ButtonProps
  extends ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean;
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Component = asChild ? Slot : "button";

    return (
      <Component
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    );
  },
);
Button.displayName = "Button";

export { Button, buttonVariants };
