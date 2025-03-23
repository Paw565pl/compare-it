import Image from "next/image";

interface ImageWithFallbackProps {
  readonly name: string;
  readonly imageUrl: string | null;
  readonly width: number;
  readonly height: number;
}

const noImagePlaceholderPath = "/no-image-placeholder.svg";

export const ImageWithFallback = ({
  name,
  imageUrl,
  width,
  height,
}: ImageWithFallbackProps) => {
  return (
    <Image
      src={imageUrl || noImagePlaceholderPath}
      onError={(e) => (e.currentTarget.src = noImagePlaceholderPath)}
      width={width}
      height={height}
      alt={name}
      className="h-auto w-auto"
    />
  );
};
