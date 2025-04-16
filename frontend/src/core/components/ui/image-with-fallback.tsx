"use client";

import { Skeleton } from "@/core/components/ui/skeleton";
import { cn } from "@/core/utils/cn";
import Image, { ImageProps } from "next/image";
import { useState } from "react";

interface ImageWithFallbackProps extends Omit<ImageProps, "src"> {
  readonly src: string | null;
  readonly containerClassName?: string;
  readonly skeletonClassName?: string;
}

const NO_IMAGE_PLACEHOLDER_PATH = "/no-image-placeholder.svg";

export const ImageWithFallback = ({
  src,
  alt,
  className,
  containerClassName,
  skeletonClassName,
  ...props
}: ImageWithFallbackProps) => {
  const [isLoading, setIsLoading] = useState(true);

  return (
    <div
      style={{
        height: props.height,
        width: props.width,
      }}
      className={cn(
        "relative flex max-w-[95svw] items-center justify-center overflow-hidden",
        containerClassName,
      )}
    >
      {isLoading && (
        <Skeleton
          className={cn("absolute inset-0 h-full w-full", skeletonClassName)}
        />
      )}
      <Image
        {...props}
        src={src || NO_IMAGE_PLACEHOLDER_PATH}
        alt={alt}
        className={cn(
          "transition-opacity duration-300 ease-in-out",
          props.fill ? "object-contain" : "",
          isLoading ? "opacity-0" : "opacity-100",
          className,
        )}
        onLoad={() => setIsLoading(false)}
        onError={(e) => {
          e.currentTarget.src = NO_IMAGE_PLACEHOLDER_PATH;
          setIsLoading(false);
        }}
      />
    </div>
  );
};
