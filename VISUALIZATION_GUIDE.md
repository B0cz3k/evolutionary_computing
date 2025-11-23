# Visualization Guide for MSLS and ILS

## Overview

The MSLS and ILS implementations now fully support visualization using your existing `SolutionVisualizer` class. You can visualize best solutions both interactively (display windows) and save them as PNG files.

## Visualization Options

### 1. RunMSLSILSExperiment (Full Experiments)

**File:** `src/main/java/org/politechnika/RunMSLSILSExperiment.java`

**Configuration flags:**
```java
boolean saveResults = true;           // Save CSV results
boolean saveVisualizations = true;    // Save PNG images
boolean showVisualizations = false;   // Display interactive windows
```

**Default behavior:**
- ✅ Saves results to `results/` directory
- ✅ Saves visualizations to `visualizations/` directory  
- ❌ Does NOT show interactive windows (set to `false` for batch processing)

**To enable interactive visualization windows:**
```java
boolean showVisualizations = true;   // Change to true
```

**Output files:**
- `visualizations/TSPA_MSLS_iterations_200_.png`
- `visualizations/TSPA_ILS_timeLimit_42567ms_pertStrength_4_runs_205_.png`
- `visualizations/TSPB_MSLS_iterations_200_.png`
- `visualizations/TSPB_ILS_timeLimit_38234ms_pertStrength_4_runs_198_.png`

### 2. TestMSLSandILS (Quick Test)

**File:** `src/main/java/org/politechnika/TestMSLSandILS.java`

**Configuration flag:**
```java
boolean visualize = false;  // Set to true to see visualizations
```

**Default behavior:**
- ❌ Does NOT show visualizations (for quick testing)

**To enable visualization:**
```java
boolean visualize = true;  // Change to true
```

**What it does:**
- Shows MSLS best solution in a window
- Shows ILS best solution in a window
- Displays both side-by-side for comparison
- Waits for you to close windows before exiting

## Running with Visualizations

### Quick Test with Visualization

```bash
# Edit TestMSLSandILS.java and set:
# boolean visualize = true;

javac -d target/classes src/main/java/org/politechnika/**/*.java
java -cp target/classes org.politechnika.TestMSLSandILS
```

This will:
1. Run 5 MSLS experiments (10 iterations each)
2. Run 5 ILS experiments (with time limit)
3. Show best MSLS solution in window
4. Show best ILS solution in window
5. Wait for you to close windows
6. Print comparison statistics

### Full Experiment with Saved Visualizations

```bash
java -cp target/classes org.politechnika.RunMSLSILSExperiment
```

This will:
1. Run 20 MSLS experiments (200 iterations each) on TSPA
2. Run 20 ILS experiments on TSPA
3. Save PNG images to `visualizations/TSPA_*.png`
4. Run 20 MSLS experiments on TSPB
5. Run 20 ILS experiments on TSPB
6. Save PNG images to `visualizations/TSPB_*.png`
7. Print all statistics

### Full Experiment with Interactive Windows

Edit `RunMSLSILSExperiment.java`:
```java
boolean showVisualizations = true;  // Change to true
```

Then run:
```bash
javac -d target/classes src/main/java/org/politechnika/**/*.java
java -cp target/classes org.politechnika.RunMSLSILSExperiment
```

**Warning:** This will open 4 windows (2 per instance). You'll need to close each window to proceed to the next instance.

## Visualization Features

The `SolutionVisualizer` displays:
- ✅ All nodes in the instance (selected and unselected)
- ✅ The tour connecting selected nodes
- ✅ Node coordinates (x, y positions)
- ✅ Algorithm name as title
- ✅ Objective value in title
- ✅ Professional graph layout

### PNG Output

Saved PNG files include:
- High-quality rendering
- Clear node markers
- Edge connections showing the tour
- Title with algorithm name and objective value
- Suitable for reports and presentations

## Comparing MSLS vs ILS Visually

### Method 1: Quick Test with Interactive Windows

```java
// In TestMSLSandILS.java, set:
boolean visualize = true;
```

Run the test and you'll see:
1. **MSLS best solution window** - Shows the best solution found by multiple random starts
2. **ILS best solution window** - Shows the best solution found by iterated perturbation

Compare visually:
- Tour structure
- Edge crossings
- Objective values in titles

### Method 2: Compare Saved PNG Files

After running the full experiment, open:
- `visualizations/TSPA_MSLS_iterations_200_.png`
- `visualizations/TSPA_ILS_timeLimit_xxxms_pertStrength_4_runs_xxx_.png`

Side-by-side comparison will show:
- ILS typically has fewer edge crossings
- ILS tour often appears more "optimized"
- Objective value difference visible in titles

## Integration with Existing Workflow

The visualization integrates seamlessly with your existing `SolutionVisualizer`:

```java
import org.politechnika.visualization.SolutionVisualizer;

// Show in window
SolutionVisualizer.show(instance, bestSolution);

// Save to file
String outputFile = "visualizations/solution.png";
SolutionVisualizer.saveToFile(instance, bestSolution, outputFile);
```

## Example Output

When running with visualizations enabled, you'll see:

```
=== BEST SOLUTIONS ===

MSLS (iterations=200):
  Objective Value: 71769.0
  Start Node: 0
  First 10 nodes: [84, 123, 45, 67, ...]
  Visualization saved: visualizations/TSPA_MSLS_iterations_200_.png

ILS (timeLimit=42567ms, pertStrength=4, runs=205):
  Objective Value: 70143.0
  Start Node: 0
  First 10 nodes: [84, 156, 23, 91, ...]
  Visualization saved: visualizations/TSPA_ILS_timeLimit_42567ms_pertStrength_4_runs_205_.png
```

## Tips for Best Results

### For Quick Testing (5 runs)
- Use `TestMSLSandILS` with `visualize = true`
- Quick feedback on algorithm behavior
- Good for debugging and demonstration

### For Report/Presentation (20 runs, 200 iterations)
- Use `RunMSLSILSExperiment` with `saveVisualizations = true`
- Generates publication-quality PNG files
- Shows best solutions from extensive search
- Can be included directly in reports

### For Interactive Exploration
- Set `showVisualizations = true` in `RunMSLSILSExperiment`
- Inspect each solution before proceeding
- Good for understanding algorithm differences
- **Note:** Requires manual window closing

## Visualization File Names

File names are automatically generated to be descriptive:

**Format:**
```
[InstanceName]_[AlgorithmName with special chars replaced].png
```

**Examples:**
```
TSPA_MSLS_iterations_200_.png
TSPA_ILS_timeLimit_42567ms_pertStrength_4_runs_205_.png
TSPB_MSLS_iterations_200_.png
TSPB_ILS_timeLimit_38234ms_pertStrength_4_runs_198_.png
```

All special characters (parentheses, equals, commas) are replaced with underscores for filesystem compatibility.

## Troubleshooting

### Issue: "No display" error
**Cause:** Running on a system without GUI support (e.g., SSH without X11)
**Solution:** 
- Set `showVisualizations = false`
- Only use `saveVisualizations = true`
- PNG files will still be generated

### Issue: Windows don't appear
**Cause:** Visualization flag not set
**Solution:** Check that `visualize = true` or `showVisualizations = true`

### Issue: Can't find PNG files
**Solution:** Check that:
1. `visualizations/` directory was created
2. `saveVisualizations = true`
3. No errors during execution

### Issue: Program hangs
**Cause:** Waiting for window to close
**Solution:** Close all visualization windows to continue

## Summary

✅ **Quick test:** `TestMSLSandILS` with `visualize = true`
✅ **Full experiment:** `RunMSLSILSExperiment` with `saveVisualizations = true`
✅ **Interactive mode:** Set `showVisualizations = true`
✅ **Batch mode:** Keep `showVisualizations = false`
✅ **Report quality:** PNG files saved to `visualizations/`
✅ **Side-by-side comparison:** Open multiple PNG files

All visualization features use your existing `SolutionVisualizer` implementation!
