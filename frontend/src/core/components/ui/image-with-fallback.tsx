"use client";

import { Skeleton } from "@/core/components/ui/skeleton";
import { cn } from "@/core/utils/cn";
import noImagePlaceholder from "@public/no-image-placeholder.svg";
import Image, { ImageProps } from "next/image";
import { useState } from "react";

interface ImageWithFallbackProps extends Omit<ImageProps, "src"> {
  readonly src: string | null;
  readonly containerClassName?: string;
  readonly skeletonClassName?: string;
  readonly isLoadingStateEnabled?: boolean;
}

export const ImageWithFallback = ({
  src,
  alt,
  className,
  containerClassName,
  skeletonClassName,
  isLoadingStateEnabled = true,
  ...props
}: ImageWithFallbackProps) => {
  const [imageSrc, setImageSrc] = useState(src || noImagePlaceholder);
  const [isLoading, setIsLoading] = useState(true);

  return (
    <div
      style={{
        height: props.height,
        width: props.width,
      }}
      className={cn(
        "relative flex max-w-full items-center justify-center overflow-hidden",
        containerClassName,
      )}
    >
      {isLoadingStateEnabled && isLoading && (
        <Skeleton
          className={cn("absolute inset-0 h-full w-full", skeletonClassName)}
        />
      )}
      <Image
        {...props}
        src={imageSrc}
        alt={alt}
        className={cn(
          "transition-opacity duration-300 ease-in-out",
          props.fill ? "object-contain" : "",
          isLoading ? "opacity-0" : "opacity-100",
          className,
        )}
        onLoad={() => setIsLoading(false)}
        onError={() => {
          setImageSrc(noImagePlaceholder);
          setIsLoading(false);
        }}
      />
    </div>
  );
};
