import { cn } from "@/core/utils/cn";
import { ComponentProps, useEffect } from "react";
import { useDebounce } from "use-debounce";

interface TextareaProps extends ComponentProps<"textarea"> {
  readonly initialWordCount?: number;
  readonly maxWordCount?: number;
}

export const Textarea = ({
  initialWordCount,
  maxWordCount,
  className,
  ...props
}: TextareaProps) => {
  const [wordCount, setWordCount] = useDebounce(initialWordCount ?? 0, 100);

  useEffect(() => {
    const value = props.value;
    if (maxWordCount && typeof value === "string")
      setWordCount(value.trim().length);
  }, [props.value, maxWordCount, setWordCount]);

  return (
    <div className="space-y-2">
      <textarea
        data-slot="textarea"
        className={cn(
          "border-input placeholder:text-muted-foreground focus-visible:border-primary focus-visible:ring-primary/50 aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive flex field-sizing-content max-h-64 min-h-32 w-full resize-y border bg-transparent px-3 py-2 text-base shadow-xs transition-[color,box-shadow] outline-none focus-visible:ring-[3px] disabled:cursor-not-allowed disabled:opacity-50 md:text-sm",
          className,
        )}
        {...props}
      />
      {maxWordCount && (
        <div className="w-full text-right text-sm text-gray-500">
          {wordCount} / {maxWordCount}
        </div>
      )}
    </div>
  );
};
