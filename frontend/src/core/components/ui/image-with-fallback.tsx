import { cn } from "@/core/utils/cn";
import Image from "next/image";

interface ImageWithFallbackProps {
  readonly name: string;
  readonly imageUrl: string | null;
  readonly width: number;
  readonly height: number;
  readonly className?: string;
}

const noImagePlaceholderPath = "/no-image-placeholder.svg";

export const ImageWithFallback = ({
  name,
  imageUrl,
  width,
  height,
  className,
}: ImageWithFallbackProps) => {
  return (
    <Image
      src={imageUrl || noImagePlaceholderPath}
      onError={(e) => (e.currentTarget.src = noImagePlaceholderPath)}
      width={width}
      height={height}
      alt={name}
      className={cn("h-auto w-auto", className)}
    />
  );
};
